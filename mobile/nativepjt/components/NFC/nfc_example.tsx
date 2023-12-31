import {useState, useEffect} from 'react';
import React from 'react';
import {View, Text, TouchableOpacity, StyleSheet, FlatList} from 'react-native';
import NfcManager, {NfcTech} from 'react-native-nfc-manager';

// Pre-step, call this before any NFC operations
NfcManager.start();

function App() {
  const [nfclist, setnfclist] = useState([]);

  async function readNdef() {
    setnfclist([]);

    try {
      await NfcManager.requestTechnology(NfcTech.Ndef);
      const tag = await NfcManager.getTag();
      setnfclist(array => [...array, tag?.id]);
    } catch (ex) {
      console.warn('Oops!', ex);
    } finally {
      await NfcManager.cancelTechnologyRequest();
    }
  }

  const renderItem = ({item}: {item: string}) => {
    return (
      <Text style={{color: 'white'}}>
        {item}
        {'\n'}
      </Text>
    );
  };

  return (
    <View style={styles.wrapper}>
      <TouchableOpacity onPress={readNdef}>
        <Text>Scan a Tag</Text>
      </TouchableOpacity>
      <FlatList
        data={nfclist}
        // contentContainerStyle={{rowGap: 12}}
        renderItem={renderItem}
        // keyExtractor={item => item}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

export default App;
