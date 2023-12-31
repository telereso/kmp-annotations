import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_annotations_client/flutter_annotations_client.dart';
import 'package:flutter_annotations_client/flutter_annotations_client_platform_interface.dart';
import 'package:flutter_annotations_client/flutter_annotations_client_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterAnnotationsClientPlatform
    with MockPlatformInterfaceMixin
    implements FlutterAnnotationsClientPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterAnnotationsClientPlatform initialPlatform = FlutterAnnotationsClientPlatform.instance;

  test('$MethodChannelFlutterAnnotationsClient is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterAnnotationsClient>());
  });

  test('getPlatformVersion', () async {
    FlutterAnnotationsClient flutterAnnotationsClientPlugin = FlutterAnnotationsClient();
    MockFlutterAnnotationsClientPlatform fakePlatform = MockFlutterAnnotationsClientPlatform();
    FlutterAnnotationsClientPlatform.instance = fakePlatform;

    expect(await flutterAnnotationsClientPlugin.getPlatformVersion(), '42');
  });
}
