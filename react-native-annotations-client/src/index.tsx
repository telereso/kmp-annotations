import { NativeModules, Platform, NativeEventEmitter, EmitterSubscription } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-annotations-client' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const AnnotationsModels = require('@telereso/annotations-models').io.telereso.annotations.models;


const AnnotationsClient = NativeModules.AnnotationsClient
  ? NativeModules.AnnotationsClient
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
    
const RocketLaunch = AnnotationsModels.RocketLaunch;
const RocketLaunchList = AnnotationsModels.RocketLaunchList;
// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const RocketLaunchListFromJson = AnnotationsModels.RocketLaunchListFromJson;

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const RocketLaunchListFromJsonArray = AnnotationsModels.RocketLaunchListFromJsonArray;
// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const RocketLaunchFromJson = AnnotationsModels.RocketLaunchFromJson;

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const RocketLaunchFromJsonArray = AnnotationsModels.RocketLaunchFromJsonArray;
const RocketLaunchToJson = AnnotationsModels.RocketLaunchToJson;

export function fetchLaunchRockets(forceReload: boolean): Promise<typeof RocketLaunch> {
  return new Promise<typeof RocketLaunch>((resolve, reject) => {
    AnnotationsClient.fetchLaunchRockets(forceReload)
      .then((data: string) => {
        resolve(RocketLaunchFromJsonArray(RocketLaunch.Companion, data));
      })
      .catch((e: any) => {
        reject(e);
      });
})      
}


export function testDefaultParam(param: String = ''): Promise<void> {
  return new Promise<void>((resolve, reject) => {
    AnnotationsClient.testDefaultParam(param)
      .then(() => {
        resolve();
      })
      .catch((e: any) => {
        reject(e);
      });
})      
}


export function getFlow(param: String = '', stream: (data: string) => void, error: (err: any) => void): EmitterSubscription {
   const eventEmitter = new NativeEventEmitter(AnnotationsClient);
  let eventListener = eventEmitter.addListener(
    'AnnotationsClient_getFlow_1ps',
    (data: string) => {
      stream(data);
    }
  );
  AnnotationsClient.getFlow(param).catch((e: any) => {
    error(e);
  }); 
  return eventListener;

}


export function getFirstRocketLaunchFlow( stream: (data: typeof RocketLaunch) => void, error: (err: any) => void): EmitterSubscription {
   const eventEmitter = new NativeEventEmitter(AnnotationsClient);
  let eventListener = eventEmitter.addListener(
    'AnnotationsClient_getFirstRocketLaunchFlow_0',
    (data: string) => {
      stream(RocketLaunchFromJson(RocketLaunch.Companion, data));
    }
  );
  AnnotationsClient.getFirstRocketLaunchFlow().catch((e: any) => {
    error(e);
  }); 
  return eventListener;

}


export function getRocketLaunchesFlow(param: String = '', stream: (data: Array<typeof RocketLaunch>) => void, error: (err: any) => void): EmitterSubscription {
   const eventEmitter = new NativeEventEmitter(AnnotationsClient);
  let eventListener = eventEmitter.addListener(
    'AnnotationsClient_getRocketLaunchesFlow_1ps',
    (data: string) => {
      stream(RocketLaunchFromJsonArray(RocketLaunch.Companion, data));
    }
  );
  AnnotationsClient.getRocketLaunchesFlow(param).catch((e: any) => {
    error(e);
  }); 
  return eventListener;

}


export function getRocketLaunchListFlow(rocketLaunch: typeof RocketLaunch, stream: (data: typeof RocketLaunchList) => void, error: (err: any) => void): EmitterSubscription {
   const eventEmitter = new NativeEventEmitter(AnnotationsClient);
  let eventListener = eventEmitter.addListener(
    'AnnotationsClient_getRocketLaunchListFlow_1rr',
    (data: string) => {
      stream(RocketLaunchListFromJson(RocketLaunchList.Companion, data));
    }
  );
  AnnotationsClient.getRocketLaunchListFlow(RocketLaunchToJson(rocketLaunch)).catch((e: any) => {
    error(e);
  }); 
  return eventListener;

}


export function getRocketLaunchFlow(param: String = '', stream: (data: Array<typeof RocketLaunch>) => void, error: (err: any) => void): EmitterSubscription {
   const eventEmitter = new NativeEventEmitter(AnnotationsClient);
  let eventListener = eventEmitter.addListener(
    'AnnotationsClient_getRocketLaunchFlow_1ps',
    (data: string) => {
      stream(RocketLaunchFromJsonArray(RocketLaunch.Companion, data));
    }
  );
  AnnotationsClient.getRocketLaunchFlow(param).catch((e: any) => {
    error(e);
  }); 
  return eventListener;

}

