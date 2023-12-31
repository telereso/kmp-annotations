
import 'flutter_annotations_client_platform_interface.dart';

class FlutterAnnotationsClient {
  Future<String?> getPlatformVersion() {
    return FlutterAnnotationsClientPlatform.instance.getPlatformVersion();
  }
}
