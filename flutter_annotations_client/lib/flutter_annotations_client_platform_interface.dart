import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_annotations_client_method_channel.dart';

abstract class FlutterAnnotationsClientPlatform extends PlatformInterface {
  /// Constructs a FlutterAnnotationsClientPlatform.
  FlutterAnnotationsClientPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterAnnotationsClientPlatform _instance = MethodChannelFlutterAnnotationsClient();

  /// The default instance of [FlutterAnnotationsClientPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterAnnotationsClient].
  static FlutterAnnotationsClientPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterAnnotationsClientPlatform] when
  /// they register themselves.
  static set instance(FlutterAnnotationsClientPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
