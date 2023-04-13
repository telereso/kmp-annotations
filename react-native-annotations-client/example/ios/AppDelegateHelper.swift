//
// Created by Ahmed Alnaami on 13/04/2023.
//

import Foundation
import AnnotationsClient
import react_native_annotations_client


@objcMembers
public class AppDelegateHelper: NSObject {

    static func appVersion() -> String {
        return Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as! String
    }

    static func appName() -> String {
        return Bundle.main.object(forInfoDictionaryKey: "CFBundleName") as! String
    }

    public func initSdks() {

        CoreClient().debugLogger()

        AnnotationsClientInstance.shared = AnnotationsClientManager.Builder(databaseDriverFactory: DatabaseDriverFactory())
                .withConfig(config: Config.Builder(appName: AppDelegateHelper.appName(), appVersion: AppDelegateHelper.appVersion()).build())
                .build()

    }

}
