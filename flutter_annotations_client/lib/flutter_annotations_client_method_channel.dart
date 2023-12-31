import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_annotations_client_platform_interface.dart';

/// An implementation of [FlutterAnnotationsClientPlatform] that uses method channels.
class MethodChannelFlutterAnnotationsClient extends FlutterAnnotationsClientPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_annotations_client');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
