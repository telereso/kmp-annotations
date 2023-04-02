import AnnotationsClient

extension String: Error {
}

@objc(AnnotationsClient)
class AnnotationsClient: RCTEventEmitter {
    private class func getManger() -> AnnotationsClientManager? {
        do {
            return try AnnotationsClientManager.Companion().getInstance()
        } catch {
            return nil
        }
    }
    // uncomment for testing, handle builder constructor if changed 
    var a = AnnotationsClientManager.Builder(databaseDriverFactory: DatabaseDriverFactory()).build()
    var manager = getManger()

    private var hasListeners = false;

    override func supportedEvents() -> [String]! {
        return ["AnnotationsClient_getFlow_1ps","AnnotationsClient_getFirstRocketLaunchFlow_0","AnnotationsClient_getRocketLaunchFlow_1ps","AnnotationsClient_getRocketLaunchArrayFlow_1ps","AnnotationsClient_getRocketLaunchListFlow_1ps","AnnotationsClient_getArrayRocketLaunchFlow_1rr"]
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
                    .onFailure { KotlinThrowable in
                        reject("fetchLaunchRockets error", "fetchLaunchRockets error", KotlinThrowable.asError())
                    }
        }
    }

    @objc(testDefaultParam:withResolver:withRejecter:)
    func testDefaultParam(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (manager == nil) {
            reject("testDefaultParam error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            resolve(manager!.testDefaultParam(param: param))
        }
    }

    @objc(getFlow:withResolver:withRejecter:)
    func getFlow(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
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
                    .onFailure { KotlinThrowable in
                        reject("getFirstRocketLaunchFlow error", "getFirstRocketLaunchFlow error", KotlinThrowable.asError())
                    }
        }
    }

    @objc(getRocketLaunchFlow:withResolver:withRejecter:)
    func getRocketLaunchFlow(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (manager == nil) {
            reject("getRocketLaunchFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            
            manager!.getRocketLaunchArrayFlow(param: param).onSuccess { streamResult in
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
                            self.sendEvent(withName: "AnnotationsClient_getRocketLaunchFlow_1ps", body: res.toJson())
                        }
                        resolve(true)
                    }
                    .onFailure { KotlinThrowable in
                        reject("getRocketLaunchFlow error", "getRocketLaunchFlow error", KotlinThrowable.asError())
                    }
        }
    }

    @objc(getRocketLaunchArrayFlow:withResolver:withRejecter:)
    func getRocketLaunchArrayFlow(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (manager == nil) {
            reject("getRocketLaunchArrayFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            
            manager!.getRocketLaunchArrayFlow(param: param).onSuccess { streamResult in
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
                            self.sendEvent(withName: "AnnotationsClient_getRocketLaunchArrayFlow_1ps", body: res.toJson())
                        }
                        resolve(true)
                    }
                    .onFailure { KotlinThrowable in
                        reject("getRocketLaunchArrayFlow error", "getRocketLaunchArrayFlow error", KotlinThrowable.asError())
                    }
        }
    }

    @objc(getRocketLaunchListFlow:withResolver:withRejecter:)
    func getRocketLaunchListFlow(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (manager == nil) {
            reject("getRocketLaunchListFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            
            manager!.getRocketLaunchListFlow(param: param).onSuccess { streamResult in
                        guard let stream = streamResult else {
                            return
                        }
                        stream.watch { (result: RocketLaunchList?, error: ClientException?) in
                            if(error != nil){
                                return
                            }
                            guard let res = result else {
                                return
                            }
                            self.sendEvent(withName: "AnnotationsClient_getRocketLaunchListFlow_1ps", body: res.toJson())
                        }
                        resolve(true)
                    }
                    .onFailure { KotlinThrowable in
                        reject("getRocketLaunchListFlow error", "getRocketLaunchListFlow error", KotlinThrowable.asError())
                    }
        }
    }

    @objc(getArrayRocketLaunchFlow:withResolver:withRejecter:)
    func getArrayRocketLaunchFlow(rocketLaunch: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
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
                    .onFailure { KotlinThrowable in
                        reject("getArrayRocketLaunchFlow error", "getArrayRocketLaunchFlow error", KotlinThrowable.asError())
                    }
        }
    }

    @objc(testSkip:withResolver:withRejecter:)
    func testSkip(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (manager == nil) {
            reject("testSkip error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            resolve(manager!.testSkip(param: param))
        }
    }

}