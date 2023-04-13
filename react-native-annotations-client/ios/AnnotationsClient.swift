import AnnotationsClient

extension String: Error {
}

public class AnnotationsClientInstance {
   public static var shared : AnnotationsClientManager? = nil
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
        if (AnnotationsClientInstance.shared == nil) {
            reject("fetchLaunchRockets error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            AnnotationsClientInstance.shared!.fetchLaunchRockets(forceReload: forceReload ? true : false)
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
        if (AnnotationsClientInstance.shared == nil) {
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

            AnnotationsClientInstance.shared!.fetchLaunchRocketsByType(type: typeValue)
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

    @objc(testDefaultParam:withResolver:withRejecter:)
    func testDefaultParam(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (AnnotationsClientInstance.shared == nil) {
            reject("testDefaultParam error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            resolve(AnnotationsClientInstance.shared!.testDefaultParam(param: param))
        }
    }

    @objc(getFlow:withResolver:withRejecter:)
    func getFlow(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (AnnotationsClientInstance.shared == nil) {
            reject("getFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            AnnotationsClientInstance.shared!.getFlow(param: param).watch { (result: NSString?, error: ClientException?) in
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
        if (AnnotationsClientInstance.shared == nil) {
            reject("getFirstRocketLaunchFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            
            AnnotationsClientInstance.shared!.getFirstRocketLaunchFlow().onSuccess { streamResult in
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
        if (AnnotationsClientInstance.shared == nil) {
            reject("getRocketLaunchesFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            
            AnnotationsClientInstance.shared!.getRocketLaunchesArrayFlow(param: param).onSuccess { streamResult in
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
        if (AnnotationsClientInstance.shared == nil) {
            reject("getArrayRocketLaunchFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            
            AnnotationsClientInstance.shared!.getArrayRocketLaunchFlow(rocketLaunch: RocketLaunch.companion.fromJson(json: rocketLaunch)).onSuccess { streamResult in
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

    @objc(testSkip:withResolver:withRejecter:)
    func testSkip(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (AnnotationsClientInstance.shared == nil) {
            reject("testSkip error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            resolve(AnnotationsClientInstance.shared!.testSkip(param: param))
        }
    }

}