import AnnotationsClient

extension String: Error {
}

@objc(AnnotationsClient)
class AnnotationsClient: NSObject {
    private class func getManger() -> AnnotationsClientManager? {
        do {
            return try AnnotationsClientManager.Companion().getInstance()
        } catch {
            return nil
        }
    }
    // uncomment for testing, handle builder constructor if changed 
    //var a = AnnotationsClientManager.Builder(databaseDriverFactory: DatabaseDriverFactory()).build()
    var manager = getManger()

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
            
            resolve(manager!.getFlow(param: param))
        }
    }

    @objc(getFirstRocketLaunchFlow:withRejecter:)
    func getFirstRocketLaunchFlow(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (manager == nil) {
            reject("getFirstRocketLaunchFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            manager!.getFirstRocketLaunchFlow()
                    .onSuccess { result in
                        guard let res = result else {
                            return
                        }
                        resolve("")
                    }
                    .onFailure { KotlinThrowable in
                        reject("getFirstRocketLaunchFlow error", "getFirstRocketLaunchFlow error", KotlinThrowable.asError())
                    }
        }
    }

    @objc(getRocketLaunchesFlow:withResolver:withRejecter:)
    func getRocketLaunchesFlow(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (manager == nil) {
            reject("getRocketLaunchesFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            manager!.getRocketLaunchesFlow(param: param)
                    .onSuccess { result in
                        guard let res = result else {
                            return
                        }
                        resolve("")
                    }
                    .onFailure { KotlinThrowable in
                        reject("getRocketLaunchesFlow error", "getRocketLaunchesFlow error", KotlinThrowable.asError())
                    }
        }
    }

    @objc(getRocketLaunchListFlow:withResolver:withRejecter:)
    func getRocketLaunchListFlow(rocketLaunch: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (manager == nil) {
            reject("getRocketLaunchListFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            manager!.getRocketLaunchListFlow(rocketLaunch: RocketLaunch.companion.fromJson(json: rocketLaunch))
                    .onSuccess { result in
                        guard let res = result else {
                            return
                        }
                        resolve("")
                    }
                    .onFailure { KotlinThrowable in
                        reject("getRocketLaunchListFlow error", "getRocketLaunchListFlow error", KotlinThrowable.asError())
                    }
        }
    }

    @objc(getRocketLaunchFlow:withResolver:withRejecter:)
    func getRocketLaunchFlow(param: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        if (manager == nil) {
            reject("getRocketLaunchFlow error", "AnnotationsClientManager was not initialized", "AnnotationsClientManager was not initialized")
        } else {
            
            manager!.getRocketLaunchFlow(param: param)
                    .onSuccess { result in
                        guard let res = result else {
                            return
                        }
                        resolve("")
                    }
                    .onFailure { KotlinThrowable in
                        reject("getRocketLaunchFlow error", "getRocketLaunchFlow error", KotlinThrowable.asError())
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