import React, {useState} from 'react';
import {View, Text, Button, NativeModules} from 'react-native';

const App = () => {
  const [pipStatus, setPipStatus] = useState('');

  const checkPiPStatus = async () => {
    try {
      const status = await NativeModules.PiPEventEmitter.checkPiPStatus();
      setPipStatus(status);
    } catch (error) {
      console.error('Failed to check PiP status:', error);
    }
  };

  return (
    <View>
      <Button title="Check PiP Status" onPress={checkPiPStatus} />
      <Text>PiP Status: {pipStatus}</Text>
    </View>
  );
};

export default App;
