///////////////////////////////////////////////////////////////////////////////
import {useState, useEffect, useRef} from 'react';

import NfcManager, {NfcEvents} from 'react-native-nfc-manager';
import {
  Text,
  Button,
  NativeModules,
  NativeEventEmitter,
  Alert,
  PermissionsAndroid,
  Platform,
} from 'react-native';

import BleManager, {
  BleScanCallbackType,
  BleScanMatchMode,
  BleScanMode,
  Peripheral,
} from 'react-native-ble-manager';

import {data_templat, our_beacon, temp_data} from './interface';
///////////////////////////////////////////////////////////////////////////////
//ble신호 스캔 시작 함수
const startScan = () => {
  try {
    console.debug('[startScan] starting scan...');
    BleManager.scan(SERVICE_UUIDS, SECONDS_TO_SCAN_FOR, ALLOW_DUPLICATES, {
      matchMode: BleScanMatchMode.Sticky,
      scanMode: BleScanMode.LowLatency,
      callbackType: BleScanCallbackType.AllMatches,
    })
      .then(() => {
        console.debug('[startScan] scan promise returned successfully.');
      })
      .catch(err => {
        console.error('[startScan] ble scan returned in error', err);
      });
  } catch (error) {
    console.error('[startScan] ble scan error thrown', error);
  }
};
//권환 요청후 권한 요청 결과에따라 처리하는 함수
const getAndroidPermission = (): Promise<void> => {
  return new Promise<void>((resolve, reject) => {
    if (Platform.OS === 'android' && Platform.Version >= 31) {
      PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      ]).then(result => {
        if (result) {
          resolve();
          console.debug(
            '[getAndroidPermission] User accepts runtime permissions android 12+',
          );
        } else {
          reject();
          console.error(
            '[getAndroidPermission] User refuses runtime permissions android 12+',
          );
        }
      });
    }
  });
};
///////////////////////////////////////////////////////////////////////////////
const BleManagerModule = NativeModules.BleManager;
const bleManagerEmitter = new NativeEventEmitter(BleManagerModule);

const SECONDS_TO_SCAN_FOR = 7;
const SERVICE_UUIDS: string[] = [];
const ALLOW_DUPLICATES = false;

function App(nurse: string, hospital: string) {
  const [read_data, setread_data] = useState<data_templat>(
    new data_templat(nurse),
  );
  const [infostatus, setinfostatus] = useState<Number>(0);
  const [beacon, setbeacon] = useState<string[]>([]);
  const init = useRef(true);
  const isblescan = useRef(false);

  //저장된 정보를 초기화
  async function rescan() {
    isblescan.current = false;
    setread_data(new data_templat(nurse));
    setinfostatus(0);

    Alert.alert('다시 스캔', '스캔을 다시 시작합니다.');
  }

  //인식된 태그의 정보 처리 함수
  const nfctagsave = tag => {
    //데이터 받아오기
    temp_data(tag.id).then(nfcdata => {
      //조건 분기 환자인 경우
      if (nfcdata.type === 'patient') {
        console.log(nfcdata.data.name);
        //데이터 저장
        setread_data(data => data.set_patient(nfcdata.data.name));
        //비트연산으로 데이터 저장 상태 갱신
        setinfostatus(num => num | 0b01);
      }
      // 조건 분기 장비인 경우
      else if (nfcdata.type === 'device') {
        console.log(nfcdata.data.name);
        //데이터 저장
        setread_data(data => data.set_device(nfcdata.data.name));
        //비트연산으로 데이터 저장 상태 갱신
        setinfostatus(num => num | 0b10);
      }
    });
  };

  const whenscanstopped = () => {
    console.debug('[ScanStop] scan is stopped.');
    BleManager.getDiscoveredPeripherals([])
      .then(peripheralsArray => {
        const closestbeacon = peripheralsArray
          .filter(device => beacon.includes(device.id))
          .reduce((prev, current) => {
            return prev.rssi > current.rssi ? prev : current;
          });
        setread_data(data => data.set_location(closestbeacon.id));
        setinfostatus(num => num | 0b100);
      })
      .catch(err => {
        return err;
      });
  };

  useEffect(() => {
    console.log('useEffect');
    // 최초 랜더링 시에만 실행
    if (init.current) {
      //현재 병원의 비콘 받아오기
      our_beacon(hospital).then((beaconlist: string[]) => {
        setbeacon(beaconlist);
      });
      //Nfc시작 및 스캔 이벤트 등록
      NfcManager.start();
      NfcManager.setEventListener(NfcEvents.DiscoverTag, nfctagsave);
      //최초 실행을 위한 init처리
      init.current = false;
    }
    //nfc스캔 시작(렌더링시 시작하기 위함)
    try {
      NfcManager.registerTagEvent();
      console.debug('[NFCStart] nfc scan start');
    } catch (ex) {
      console.log('ex', ex);
    }

    //데이터 조건 충족시 nfc스캔 중지
    if ((infostatus & 0b11) === 0b11) {
      console.debug('[NFCStop]nfc scan stop');
      NfcManager.unregisterTagEvent();
    }

    if (infostatus === 0b111) {
      Alert.alert('3가지 정보 스캔 완료');
    }

    //ble스캔 시작을 위한 전처리
    try {
      BleManager.start({showAlert: false})
        .then(() => console.debug('BleManager started.'))
        .catch(error =>
          console.error('BeManager could not be started.', error),
        );
    } catch (error) {
      console.error('unexpected error starting BleManager.', error);
      return;
    }

    //ble 스캔 종료시 실행할 함수 이벤트 등록
    const listeners = [
      bleManagerEmitter.addListener('BleManagerStopScan', whenscanstopped),
    ];

    //ble스캔을 안했다면 스캔
    if (!isblescan.current) {
      //ble 통신을 위한 권한 요청
      getAndroidPermission()
        .then(() => {
          isblescan.current = true;
          //ble스캔 시작
          startScan();
        })
        .catch(() => {
          console.debug('permission error');
        });
    }

    return () => {
      // ble 이벤트 제거
      for (const listener of listeners) {
        listener.remove();
      }
    };
  }, [read_data, infostatus]);

  return (
    <>
      <Text>{'\n\n\n\n'}</Text>
      <Text> 환자 : {read_data.patient}</Text>
      <Text> 장비 : {read_data.device}</Text>
      <Text> 장소 : {read_data.location}</Text>
      <Button
        onPress={rescan}
        title="스캔 다시하기"
        color="#841584"
        accessibilityLabel="Learn more about this purple button"
      />
    </>
  );
}

export default App;