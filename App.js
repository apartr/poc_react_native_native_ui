import React, {useCallback, useEffect, useRef, useState} from 'react';

import CameraPreview from './components/CameraPreview';
import {PERMISSIONS, check, request} from 'react-native-permissions';
import {
  View,
  TouchableOpacity,
  NativeEventEmitter,
  NativeModules,
  StyleSheet,
  Text,
  UIManager,
  findNodeHandle,
  Image,
} from 'react-native';
import {Camera, useCameraDevices} from 'react-native-vision-camera';
// import {NativeModules} from "react-native"

// const createFragment = viewId =>
//   UIManager.dispatchViewManagerCommand(
//     viewId,
//     // we are calling the 'create' command
//     UIManager.MyViewManager.Commands.create.toString(),
//     [viewId],
//   );

const App = () => {
  const [allowToRender, setAllowToRender] = useState(false);
  const [previewImage, setPreviewImage] = useState(false);
  const devices = useCameraDevices();
  const device = devices.front;
  // const {hasPermission} = useCameraPermission();

  const cameraRef = useRef(null);

  // useEffect(() => {
  //   // Start the camera
  //   cameraRef.current.startCamera();

  //   // Listen for image captured event
  //   const eventEmitter = new NativeEventEmitter(NativeModules.CameraView);
  //   eventEmitter.addListener('onImageCaptured', handleImageCaptured);

  //   return () => {
  //     // Clean up event listener
  //     eventEmitter.removeListener('onImageCaptured', handleImageCaptured);
  //   };
  // }, []);

  const handleImageCaptured = base64Image => {
    console.log('Image captured:', base64Image);
    // Handle base64 image data
  };

  useEffect(() => {
    check(PERMISSIONS.ANDROID.CAMERA).then(response => {
      console.log('response', response);
      if (response === 'granted') {
        setAllowToRender(true);
      } else {
        request(PERMISSIONS.ANDROID.CAMERA).then(response2 => {
          console.log(response2);
          if (response2 === 'granted') {
            setAllowToRender(true);
          }
        });
      }
    });
  }, []);

  const handleTakeSnapshot = useCallback(async () => {
    const photo = await cameraRef.current?.takeSnapshot();
    setPreviewImage('file://' + photo.path);
  }, []);

  if (!allowToRender) {
    return null;
  }

  if (device == null) {
    return null;
  }

  const multiple = 10;
  const fullScreen = {
    position: 'absolute',
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
  };
  const style = {
    // width: 30 * multiple,
    // height: 40 * multiple,
    // flex: 1,
    ...fullScreen,
    backgroundColor: 'lightgrey',
  };
  // const style = StyleSheet.absoluteFill;

  // console.log('device', device);
  const oldLibs = 1;

  // const SomeAction = () => {
  //   cameraRef.current.startCamera()
  //   // Get FaceTec

  //   cameraRef.current.dataSet({
  //     action: turn_left,
  //     duration: 3
  //   })

  //   cameraRef.current.validateDataSet()
  // }

  if (previewImage) {
    return (
      <View style={{flex: 1}}>
        <Image source={{uri: previewImage}} style={{flex: 1}} />
        <View style={{position: 'absolute', left: 0, bottom: 0, right: 0}}>
          <TouchableOpacity
            style={{
              borderWidth: 1,
              padding: 30,
              backgroundColor: 'black',
              alignItems: 'center',
            }}
            onPress={() => setPreviewImage('')}>
            <Text style={{color: 'red', fontWeight: 'bold'}}>
              {'Re-Take Snapshot'}
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  return (
    <View style={{flex: 1}}>
      <Camera
        ref={cameraRef}
        style={style}
        device={device}
        isActive={true}
        photo={true}
      />
      <View style={{flex: 1}} />
      <TouchableOpacity
        style={{
          borderWidth: 1,
          padding: 30,
          backgroundColor: 'black',
          alignItems: 'center',
        }}
        onPress={handleTakeSnapshot}>
        <Text style={{color: 'green', fontWeight: 'bold'}}>
          {'Take Snapshot'}
        </Text>
      </TouchableOpacity>
    </View>
  );

  // if (oldLibs) {

  // } else {
  //   return (
  //     <View
  //       style={{
  //         flex: 1,
  //       }}>
  //       <CameraPreview
  //         ref={cameraRef}
  //         style={[style]}
  //         onChange={event => {
  //           // console.log(
  //           //   'event.nativeEvent.message',
  //           //   event.nativeEvent.message?.MOUTH_BOTTOM_X,
  //           //   event.nativeEvent.message?.MOUTH_BOTTOM_Y,
  //           // );
  //         }}
  //       />
  //       <TouchableOpacity
  //         style={{borderWidth: 1, padding: 30}}
  //         onPress={() => {
  //           // cameraRef.current?.captureImage();
  //           // console.log('cameraRef', cameraRef.current?.captureImage());
  //           // NativeModules.TestView.testMethod(
  //           //   findNodeHandle(cameraRef.current),
  //           // );
  //           console.log('cameraRef', NativeModules.CameraPreview.testMethod());
  //         }}>
  //         <Text>{'TEST 1'}</Text>
  //       </TouchableOpacity>
  //     </View>
  //   );
  // }
};

export default App;
