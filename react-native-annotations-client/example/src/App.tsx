import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import {
  fetchLaunchRockets, getFirstRocketLaunchFlow,
  getFlow,
  getRocketLaunchFlow, getRocketLaunchListFlow,
} from 'react-native-annotations-client';

const AnnotationsModels = require('@telereso/annotations-models').io.telereso
  .annotations.models;

const RocketLaunch = AnnotationsModels.RocketLaunch;

export default function App() {
  const [rockets, setRockets] = React.useState<Array<typeof RocketLaunch> | []>(
    []
  );
  React.useEffect(() => {
    fetchLaunchRockets(true)
      .then((data) => {
        setRockets(data);
      })
      .catch((e) => {
        console.log(e);
      });

    const l = getFlow(
      'aaaa,ffff,ssss,ddd,zzzzz',
      (data: string) => {
        console.log(data);
      },
      (err: any) => {
        console.error(err);
      }
    );

    const l2 = getRocketLaunchFlow(
      '',
      (data) => {
        console.log('getRocketLaunchFlow', data[0]);
      },
      (err) => {
        console.log(err);
      }
    );

    const l4 = getFirstRocketLaunchFlow(
      (data) => {
        console.log('getFirstRocketLaunchFlow', data);
      },
      (err) => {
        console.log(err);
      }
    );

    return () => {
      l.remove();
      l2.remove();
      l4.remove();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text>🚀 Total Rockets Launched: {rockets?.length ?? 'loading'} </Text>
      <Text>
        First Rocket Mission:: {rockets[0]?.mission_name ?? 'loading'}{' '}
      </Text>
      <Text>First Rocket name:: {rockets[0]?.rocket?.name ?? 'loading'} </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
