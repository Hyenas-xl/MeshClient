package org.embest.ali;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.embest.mesh.ConnectHandle;
import org.embest.mesh.DeviceStatusListener;
import org.embest.mesh.HeartBeatHandler;
import org.embest.mesh.signal.Command;
import org.embest.mesh.signal.CommandCallBack;
import org.embest.mesh.signal.CommandRequestCallBack;
import org.embest.mesh.signal.DeviceStatusCallBack;
import org.embest.model.mesh.Device;
import org.embest.util.BASE64;
import org.embest.util.LogUtils;
import org.embest.util.TaskUtil;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.Variant;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ServiceHandle implements Consumer<String> {
	private static final Logger logger = LogUtils.getLogger("ServiceHandle");
	private final Lock lock = new ReentrantLock();
	private Map<String, Condition> provsionMap = new HashMap<String, Condition>();
	private String obcode;
	private UInt16 unicast;
	private boolean provsionStatus = false;

	enum Ali_Service_Type {
		scan, provision, configPub, configSub, subDevice
	}

	private Command command;

	public void setCommand(Command command) {
		this.command = command;
		this.command.deviceStateListen(new DeviceStatusListener());
	}

	@Override
	public void accept(String value) {
		if (value != null) {
			logger.info("Cloud command: "+value);
			JSONObject obj = (JSONObject) JSON.parse(value);
			String type = obj.getString("type");

			if (type != null) {
				Ali_Service_Type serviceType = Ali_Service_Type.valueOf(type);
				switch (serviceType) {
				case scan:
					String scanKey = obj.getString("key");
					String scanAction = obj.getString("action");
					scan(scanKey, scanAction);
					break;
				case provision:
					String pUuid = obj.getString("uuid");
					if (obj.containsKey("obcode")) {
						lock.lock();
						obcode = obj.getString("obcode");
						try {
							if (provsionMap.containsKey(pUuid)) {
								Condition condition = provsionMap.remove(pUuid);
								condition.signal();
							}
						} finally {
							lock.unlock();
						}
					} else {
						String pKey = obj.getString("key");
						String pName = obj.getString("name");
						provision(pUuid, pName, pKey);
					}
					break;
				case configPub:
					String deviceNamePub = obj.getString("deviceName");
					String unicastPub = obj.getString("unicast");
					String pubAddr = obj.getString("pubAddr");
					String modelPub = obj.getString("model");
					int period = obj.getIntValue("period");
					int elementPub = obj.getIntValue("element");
					int server = obj.getIntValue("server");
					JSONArray arraySub = obj.getJSONArray("subAddrs");
					List<String> subAddressSub = null;
					if (arraySub != null) {
						subAddressSub = arraySub.toJavaList(String.class);
					}
					configPub(deviceNamePub,unicastPub, pubAddr, modelPub, period, elementPub,server,subAddressSub);
					break;
				case configSub:
					String unicastSub = obj.getString("unicast");
					int elementSub = obj.getIntValue("element");
					String modelSub = obj.getString("model");
					JSONArray array = obj.getJSONArray("subAddrs");
					if (array != null) {
						List<String> subAddress = array.toJavaList(String.class);
						if (subAddress != null && subAddress.size() > 0) {
							configSub(unicastSub, elementSub, modelSub, subAddress);
						}
					}
					break;
				case subDevice:
					String deviceName = obj.getString("deviceName");
					String deviceSecret = obj.getString("deviceSecret");
					addSubDevice(deviceName, BASE64.decryptBASE64(deviceSecret));
					break;
				}
			}
		}
	}

	public void connect() {
		ConnectHandle.instance.connectNet();
	}

	void addSubDevice(String deviceName, String deviceSecret) {
		AliKitService.kit().addSubDevice(deviceName, deviceSecret);
	}

//	void subdeviceOnline() {
//		command.deviceOnline(new DeviceStatusCallBack() {
//
//			@Override
//			public void hanle(String name, boolean status, String error) {
//				if (status) {
//					AliKitService.kit().loginSubDevice(name);
//				} else {
//					AliKitService.kit().logoutSubDevice(name);
//				}
//				logger.severe(name + " online [" + status + "]: " + error);
//			}
//		});
//	}

	void scan(String key, String action) {
		if (command != null) {
			TaskUtil.instance.add(() -> {
				try {
					if ("on".equals(action)) {
						command.startScan(new CommandCallBack<Device>() {

							@Override
							public void execute(Device value, String error) {
								if (value != null) {
									AliKitService.kit().postScanInfo(value.getName(), value.getUuid(), key);
								}
							}
						});
					} else {
						command.stopScan();
					}
				} catch (Exception e) {
					AliKitService.kit().postScanError(key, e.getMessage());
				}
			});
		}
	}

	void provision(String uuid, String name, String key) {
		if (command != null) {
			TaskUtil.instance.add(() -> {
				command.requestkey(new CommandRequestCallBack<String>() {

					@Override
					public String execute(String value, String error) {
						AliKitService.kit().postRequestKey(uuid, name, key);
						Condition condition = lock.newCondition();
						provsionMap.put(uuid, condition);
						lock.lock();
						try {
							condition.await(30, TimeUnit.SECONDS);
						} catch (Exception e) {
						} finally {
							if (provsionMap.containsKey(uuid)) {
								provsionMap.remove(uuid);
							}
							lock.unlock();
						}
						return obcode;
					}
				});
				ConnectHandle.instance.setReconnected(false);
				command.provision(uuid, new CommandCallBack<UInt16>() {

					@Override
					public void execute(UInt16 value, String error) {
						if (error != null) {
							logger.severe("Provision [" + uuid + "] error: " + error);
							AliKitService.kit().postProvisionError(uuid, name, key, error);
						} else if (value != null) {
							unicast = value;
							lock.lock();
							try {
								Condition condition = lock.newCondition();
								provsionMap.put(uuid, condition);
								condition.await(5, TimeUnit.SECONDS);
							} catch (InterruptedException e) {
							} finally {
								if (provsionMap.containsKey(uuid)) {
									provsionMap.remove(uuid);
								}
								lock.unlock();
							}
							if (!provsionStatus) {
								ConnectHandle.instance.connectNode(unicast, status ->{
									if(status){
										command.target(unicast, new CommandCallBack<Boolean>() {
											
											@Override
											public void execute(Boolean value, String error) {
												if(value != null && value){
													command.composition(new CommandCallBack<String>() {

														@Override
														public void execute(String value, String error) {
															if(value == null){
																AliKitService.kit().postProvisionError(uuid, name, key, error);
																reset();
																return;
															}
															
															AliKitService.kit().postProvision(uuid, unicast.toString(), value,
																	name, key);

															command.target(unicast, new CommandCallBack<Boolean>() {

																@Override
																public void execute(Boolean value, String error) {
																	command.appkeyAdd(new UInt16(1),
																			new CommandCallBack<Boolean>() {

																		@Override
																		public void execute(Boolean value, String error) {
																			if(value != null && value){
																				HeartBeatHandler.instance.listenDevice(unicast);
																			}
																			logger.info("App key add status: "+value+ ",error: " + error);
																		}
																	});
																}
															});
														}
													});
												}else{
													logger.severe("Target failed!");
													AliKitService.kit().postProvisionError(uuid, name, key, "Connect failed,node reset.");
													reset();
												}
											}
										});
									}else{
										logger.severe("connect failed!");
										AliKitService.kit().postProvisionError(uuid, name, key, "Connect failed,node reset.");
										reset();
									}
								});
							}
						}
					}
				}, new CommandCallBack<String>() {
					@Override
					public void execute(String value, String error) {
						logger.info("Provision [" + uuid + "] error: " + error);
						lock.lock();
						try {
							if (provsionMap.containsKey(uuid)) {
								provsionStatus = true;
								provsionMap.remove(uuid).signal();
							}
						} finally {
							lock.unlock();
						}

						if (error != null && error.length() > 0) {
							AliKitService.kit().postProvisionError(uuid, name, key, error);
							reset();
						} else if (value != null && unicast != null) {
							AliKitService.kit().postProvision(uuid, unicast.toString(), value, name, key);

							command.target(unicast, new CommandCallBack<Boolean>() {

								@Override
								public void execute(Boolean value, String error) {
									command.appkeyAdd(new UInt16(1), new CommandCallBack<Boolean>() {

										@Override
										public void execute(Boolean value, String error) {
											if(value != null && value){
												HeartBeatHandler.instance.listenDevice(unicast);
											}
											logger.info("App key add status: " + value + ",error: " + error);
										}
									});
								}
							});
						}
					}
				});

			});
		}
	}
	
	void config(UInt16 address,Consumer<Boolean> consumer){
//		ConnectHandle.instance.disconnect();
		ConnectHandle.instance.connectNode(address, status ->{
			consumer.accept(status);
			
			ConnectHandle.instance.cleanIdentityConsumer();
		});
	}

	void configPub(String deviceName,String unicast, String pubAddr, String model, int period, int element,int server,List<String> subAddress) {
		if (command != null) {
			TaskUtil.instance.add(() -> {
				UInt16 address = new UInt16(unicast);
				config(address,status ->{
					if(status){
						command.target(address, new CommandCallBack<Boolean>() {

							@Override
							public void execute(Boolean value, String error) {
								if (value != null && value) {
									Map<String, Variant> info = new HashMap<>();
									int modelId = Integer.parseInt(model, 16);
									info.put("ele_idx", new Variant<UInt16>(new UInt16(element)));
									info.put("app_idx", new Variant<UInt16>(new UInt16(1)));
									info.put("mod_id", new Variant<UInt16>(new UInt16(modelId)));

									command.bind(info, new CommandCallBack<Boolean>() {

										@Override
										public void execute(Boolean value, String error) {
											if (value != null && value) {
												Map<String, Variant> info = new HashMap<>();
												int eleAddr = address.intValue() + element;
												int pub = Integer.parseInt(pubAddr, 16);
												
												info.put("ele_addr", new Variant<UInt16>(new UInt16(eleAddr)));
												info.put("pub_addr", new Variant<UInt16>(new UInt16(pub)));
												info.put("app_idx", new Variant<UInt16>(new UInt16(1)));
												info.put("period", new Variant<UInt16>(new UInt16(period)));
												info.put("mod_id",new Variant<UInt16>(new UInt16(modelId)));

												command.pubSet(info, new CommandCallBack<Boolean>() {

													@Override
													public void execute(Boolean value, String error) {
														if (value != null && value) {
															logger.info("pub set ok ");
															Config.instance.setDeviceAddress(deviceName, pub,address.intValue(),element,modelId,server);
															if(server == 1){
																Map<String, Variant> subInfo = new HashMap<>();
																subInfo.put("ele_addr", new Variant<UInt16>(new UInt16(eleAddr)));
																subInfo.put("sub_addr", new Variant<UInt16>(new UInt16(pub)));
																subInfo.put("app_idx", new Variant<UInt16>(new UInt16(1)));
																subInfo.put("mod_id",new Variant<UInt16>(new UInt16(modelId)));
																command.subAdd(subInfo, new CommandCallBack<Boolean>() {
																	
																	@Override
																	public void execute(Boolean value, String error) {
//																		if(value != null && value){
//																			HeartBeatHandler.instance.listenDevice(address);
//																		}
																		logger.info("add sub :"+subInfo.get("sub_addr").getValue().toString());
																		if(value != null && value){
																			addSubAddress(subInfo,subAddress);
																		}
																	}
																});
															}
														} else {
															logger.severe("pub set : " + error);
															ConnectHandle.instance.connectNet();
														}
													}
												});
											} else {
												logger.severe("bind : " + error);
												ConnectHandle.instance.connectNet();
											}
										}
									});
								} else {
									logger.severe("target : " + error);
									ConnectHandle.instance.connectNet();
								}
							}
						});
					}
				});
			});
		}
	}
	void addSubAddress(Map<String, Variant> subInfo,List<String> array){
		if(array != null && array.size() > 0){
			subInfo.put("sub_addr", new Variant<UInt16>(new UInt16(Integer.parseInt(array.remove(0), 16))));
			command.subAdd(subInfo, new CommandCallBack<Boolean>() {
				
				@Override
				public void execute(Boolean value, String error) {
					logger.info("add sub :"+subInfo.get("sub_addr").getValue().toString());
					if(value !=null && value){
						addSubAddress(subInfo,array);
					}
				}
			});
		}
	}
	void configSub(String unicast, int element, String model, List<String> subAddress) {
		if (command != null) {
			TaskUtil.instance.add(() -> {
				UInt16 address = new UInt16(unicast);
				config(address,status ->{
					command.target(address, new CommandCallBack<Boolean>() {

						@Override
						public void execute(Boolean value, String error) {
							if (value != null && value) {
								Map<String, Variant> info = new HashMap<>();
								int modelId = Integer.parseInt(model, 16);
								info.put("ele_idx", new Variant<UInt16>(new UInt16(element)));
								info.put("app_idx", new Variant<UInt16>(new UInt16(1)));
								info.put("mod_id", new Variant<UInt16>(new UInt16(modelId)));

								command.bind(info, new CommandCallBack<Boolean>() {

									@Override
									public void execute(Boolean value, String error) {
										if (value != null && value) {
											int eleAddrInt = address.intValue() + element;
											Variant<UInt16> eleAddr = new Variant<UInt16>(new UInt16(eleAddrInt));
											Variant<UInt16> appInx = new Variant<UInt16>(new UInt16(1));
											if (subAddress != null && subAddress.size() > 0) {
												subAddress.forEach(subAddr -> {

													Map<String, Variant> info = new HashMap<>();
													int pub = Integer.parseInt(subAddr, 16);
													info.put("ele_addr", eleAddr);
													info.put("sub_addr", new Variant<UInt16>(new UInt16(pub)));
													info.put("app_idx", appInx);
													info.put("mod_id",new Variant<UInt16>(new UInt16(modelId)));

													command.subAdd(info, new CommandCallBack<Boolean>() {

														@Override
														public void execute(Boolean value, String error) {
															if (value != null && value) {
																logger.info("sub add ok");
															} else {
																logger.severe("sub add : " + error);
															}
															ConnectHandle.instance.connectNet();
														}
													});
												});
											}
										} else {
											logger.severe("bind : " + error);
											ConnectHandle.instance.connectNet();
										}
									}
								});
							} else {
								logger.severe("target : " + error);
								ConnectHandle.instance.connectNet();
							}
						}
					});
				});
			});
		}
	}

	void reset() {
		if (command != null) {
			command.nodeReset(new CommandCallBack<Boolean>() {

				@Override
				public void execute(Boolean value, String error) {
					logger.info("pub set [" + value + "] error:" + error);
				}
			});
		}
	}
}
