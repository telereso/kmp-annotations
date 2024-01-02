import AnnotationsClient

extension String: Error {
}


@objc(AnnotationsClient)
class AnnotationsClient: RCTEventEmitter {

    private var hasListeners = false;

    override func supportedEvents() -> [String]! {
        return ["AnnotationsClient_getFlow_1ps","AnnotationsClient_getFirstRocketLaunchFlow_0","AnnotationsClient_getRocketLaunchesFlow_1ps","AnnotationsClient_getArrayRocketLaunchFlow_1rr"]
    }

    override func startObserving() {
        hasListeners = true
    }

    override func stopObserving() {
        hasListeners = false
    }

    override func sendEvent(withName name: String!, body: Any!) {
        if (hasListeners) {
            super.sendEvent(withName: name, body: body)
        }
    }

    @objc(fetchLaunchRockets:withResolver:withRejecter:)
    func fetchLaunchRockets(forceReload: Bool, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let manager = AnnotationsClientManager.Companion().getInstanceOrNull()
if (manager == nil) {
            reject("fetchLaunchRockets error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            manager!.fetchLaunchRockets(forceReload: forceReload ? true : false)
                    .onSuccess { result in
                        guard let res = result else {
                            return
                        }
                        resolve(RocketLaunch.Companion().toJson(array: res))
                    }
                    .onFailure { clientException in
                        reject("fetchLaunchRockets error", "fetchLaunchRockets error", clientException.message ?? "fetchLaunchRockets error")
                    }
        }
    }

    @objc(fetchLaunchRocketsByType:withResolver:withRejecter:)
    func fetchLaunchRocketsByType(type: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let manager = AnnotationsClientManager.Companion().getInstanceOrNull()
if (manager == nil) {
            reject("fetchLaunchRocketsByType error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            let typeValue: RocketLaunch.Type_
            switch type {
            case "FIRST":
                typeValue = RocketLaunch.Type_.first
            case "SECOND":
                typeValue = RocketLaunch.Type_.second
            default:
                typeValue = RocketLaunch.Type_.first
            }

            manager!.fetchLaunchRocketsByType(type: typeValue)
                    .onSuccess { result in
                        guard let res = result else {
                            return
                        }
                        resolve(RocketLaunch.Companion().toJson(array: res))
                    }
                    .onFailure { clientException in
                        reject("fetchLaunchRocketsByType error", "fetchLaunchRocketsByType error", clientException.message ?? "fetchLaunchRocketsByType error")
                    }
        }
    }

    @objc(testEmpty:withRejecter:)
    func testEmpty(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let manager = AnnotationsClientManager.Companion().getInstanceOrNull()
if (manager == nil) {
            reject("testEmpty error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            resolve(manager!.testEmpty())
        }
    }

    @objc(testOne:withResolver:withRejecter:)
    func testOne(test: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let manager = AnnotationsClientManager.Companion().getInstanceOrNull()
if (manager == nil) {
            reject("testOne error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            resolve(manager!.testOne(test: test))
        }
    }

    @objc(testDefaultParams:name:param:param2:param3:withResolver:withRejecter:)
    func testDefaultParams(id: Int, name: String, param: String, param2: String, param3: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let manager = AnnotationsClientManager.Companion().getInstanceOrNull()
if (manager == nil) {
            reject("testDefaultParams error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            resolve(manager!.testDefaultParams(id: id, name: name, param: param, param2: param2, param3: param3))
        }
    }

    @objc(testDefaultParam:withResolver:withRejecter:)
    func testDefaultParam(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let manager = AnnotationsClientManager.Companion().getInstanceOrNull()
if (manager == nil) {
            reject("testDefaultParam error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            resolve(manager!.testDefaultParam(param: param))
        }
    }

    @objc(getFlow:withResolver:withRejecter:)
    func getFlow(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let manager = AnnotationsClientManager.Companion().getInstanceOrNull()
if (manager == nil) {
            reject("getFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            manager!.getFlow(param: param).watch { (result: NSString?, error: ClientException?) in
                        if(error != nil){
                           return reject("getFlow error", "getFlow error", error?.message ?? "empty message")
                        }
                        guard let res = result else {
                            return
                        }
                        self.sendEvent(withName: "AnnotationsClient_getFlow_1ps", body: res)
                    }
           resolve(true)
        }
    }

    @objc(getFirstRocketLaunchFlow:withRejecter:)
    func getFirstRocketLaunchFlow(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let manager = AnnotationsClientManager.Companion().getInstanceOrNull()
if (manager == nil) {
            reject("getFirstRocketLaunchFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            
            manager!.getFirstRocketLaunchFlow().onSuccess { streamResult in
                        guard let stream = streamResult else {
                            return
                        }
                        stream.watch { (result: RocketLaunch?, error: ClientException?) in
                            if(error != nil){
                                return
                            }
                            guard let res = result else {
                                return
                            }
                            self.sendEvent(withName: "AnnotationsClient_getFirstRocketLaunchFlow_0", body: res.toJson())
                        }
                        resolve(true)
                    }
                    .onFailure { clientException in
                        reject("getFirstRocketLaunchFlow error", "getFirstRocketLaunchFlow error", clientException.message ?? "getFirstRocketLaunchFlow error")
                    }
        }
    }

    @objc(getRocketLaunchesFlow:withResolver:withRejecter:)
    func getRocketLaunchesFlow(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let manager = AnnotationsClientManager.Companion().getInstanceOrNull()
if (manager == nil) {
            reject("getRocketLaunchesFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            
            manager!.getRocketLaunchesArrayFlow(param: param).onSuccess { streamResult in
                        guard let stream = streamResult else {
                            return
                        }
                        stream.watch { (result: RocketLaunchArray?, error: ClientException?) in
                            if(error != nil){
                                return
                            }
                            guard let res = result else {
                                return
                            }
                            self.sendEvent(withName: "AnnotationsClient_getRocketLaunchesFlow_1ps", body: res.toJson())
                        }
                        resolve(true)
                    }
                    .onFailure { clientException in
                        reject("getRocketLaunchesFlow error", "getRocketLaunchesFlow error", clientException.message ?? "getRocketLaunchesFlow error")
                    }
        }
    }

    @objc(getArrayRocketLaunchFlow:withResolver:withRejecter:)
    func getArrayRocketLaunchFlow(rocketLaunch: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let manager = AnnotationsClientManager.Companion().getInstanceOrNull()
if (manager == nil) {
            reject("getArrayRocketLaunchFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            
            manager!.getArrayRocketLaunchFlow(rocketLaunch: RocketLaunch.companion.fromJson(json: rocketLaunch)).onSuccess { streamResult in
                        guard let stream = streamResult else {
                            return
                        }
                        stream.watch { (result: KotlinArray<RocketLaunch>?, error: ClientException?) in
                            if(error != nil){
                                return
                            }
                            guard let res = result else {
                                return
                            }
                            self.sendEvent(withName: "AnnotationsClient_getArrayRocketLaunchFlow_1rr", body: RocketLaunch.Companion().toJson(array: res))
                        }
                        resolve(true)
                    }
                    .onFailure { clientException in
                        reject("getArrayRocketLaunchFlow error", "getArrayRocketLaunchFlow error", clientException.message ?? "getArrayRocketLaunchFlow error")
                    }
        }
    }

}