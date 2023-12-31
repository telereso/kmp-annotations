#include "include/flutter_annotations_client/flutter_annotations_client_plugin_c_api.h"

#include <flutter/plugin_registrar_windows.h>

#include "flutter_annotations_client_plugin.h"

void FlutterAnnotationsClientPluginCApiRegisterWithRegistrar(
    FlutterDesktopPluginRegistrarRef registrar) {
  flutter_annotations_client::FlutterAnnotationsClientPlugin::RegisterWithRegistrar(
      flutter::PluginRegistrarManager::GetInstance()
          ->GetRegistrar<flutter::PluginRegistrarWindows>(registrar));
}
