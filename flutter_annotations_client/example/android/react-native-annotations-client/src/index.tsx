import { NativeModules, Platform, NativeEventEmitter, EmitterSubscription } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-annotations-client' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const AnnotationsModels = require('@'telereso'/annotations-models').io.telereso.annotations.models;


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
    
const RocketLaunchArray = AnnotationsModels.RocketLaunchArray;
const RocketLaunch = AnnotationsModels.RocketLaunch;
// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const RocketLaunchArrayFromJson = AnnotationsModels.RocketLaunchArrayFromJson;

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const RocketLaunchArrayFromJsonArray = AnnotationsModels.RocketLaunchArrayFromJsonArray;
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


export function fetchLaunchRocketsByType(type: typeof RocketLaunch.Type): Promise<typeof RocketLaunch> {
  return new Promise<typeof RocketLaunch>((resolve, reject) => {
    AnnotationsClient.fetchLaunchRocketsByType(type.name)
      .then((data: string) => {
        resolve(RocketLaunchFromJsonArray(RocketLaunch.Companion, data));
      })
      .catch((e: any) => {
        reject(e);
      });
})      
}


export function testEmpty(): Promise<void> {
  return new Promise<void>((resolve, reject) => {
    AnnotationsClient.testEmpty()
      .then(() => {
        resolve();
      })
      .catch((e: any) => {
        reject(e);
      });
})      
}


export function testOne(test: String = ''): Promise<void> {
  return new Promise<void>((resolve, reject) => {
    AnnotationsClient.testOne(test)
      .then(() => {
        resolve();
      })
      .catch((e: any) => {
        reject(e);
      });
})      
}


export function testDefaultParams(id: number,name: string,param: String = '',param2: String = '',param3: String = ''): Promise<void> {
  return new Promise<void>((resolve, reject) => {
    AnnotationsClient.testDefaultParams(id, name, param, param2, param3)
      .then(() => {
        resolve();
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


export function getRocketLaunchesFlow(param: String = '', stream: (data: typeof RocketLaunchArray) => void, error: (err: any) => void): EmitterSubscription {
   const eventEmitter = new NativeEventEmitter(AnnotationsClient);
  let eventListener = eventEmitter.addListener(
    'AnnotationsClient_getRocketLaunchesFlow_1ps',
    (data: string) => {
      stream(RocketLaunchArrayFromJson(RocketLaunchArray.Companion, data));
    }
  );
  AnnotationsClient.getRocketLaunchesFlow(param).catch((e: any) => {
    error(e);
  }); 
  return eventListener;

}


export function getArrayRocketLaunchFlow(rocketLaunch: typeof RocketLaunch, stream: (data: Array<typeof RocketLaunchArray>) => void, error: (err: any) => void): EmitterSubscription {
   const eventEmitter = new NativeEventEmitter(AnnotationsClient);
  let eventListener = eventEmitter.addListener(
    'AnnotationsClient_getArrayRocketLaunchFlow_1rr',
    (data: string) => {
      stream(RocketLaunchFromJsonArray(RocketLaunch.Companion, data));
    }
  );
  AnnotationsClient.getArrayRocketLaunchFlow(RocketLaunchToJson(rocketLaunch)).catch((e: any) => {
    error(e);
  }); 
  return eventListener;

}

