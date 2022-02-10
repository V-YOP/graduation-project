export const domain = "localhost"
export const port = "8080"
export const baseURI = `${domain}:${port}`

export const baseWSURL = `ws://${baseURI}/ws`
export const getPosWSURL = `${baseWSURL}/getPos`

export const baseHttpURL = `http://${baseURI}/api`
export const adminURL = `http://${baseURI}/admin`
export const getPosesByBusIDBetweenURL = `${baseHttpURL}/getPosesByIDsBetween`;
export const getInfo = `${baseHttpURL}/getInfo`; // 目前使用这个
export const getInfos = `${baseHttpURL}/getInfos`; // 目前使用这个
export const getoverspeedPosesByAdcodeBetweenURL = `${baseHttpURL}/getOverspeedPosesByAdcodeBetween`
export const loginURL = `${adminURL}/login`
export const changePasswdURL = `${adminURL}/changePasswd`
export const changeNameURL = `${adminURL}/changeName`

/**
 * @deprecated
 */
export const getBusAndLineInfoURL = `${baseHttpURL}/getBusAndAllNodeInfo`
export const getBusAndLineInfoHistoryURL = `${baseHttpURL}/getBusAndAllNodeInfoHistory`
