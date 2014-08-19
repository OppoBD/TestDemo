/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#include <jni.h>
#include <string.h>
#include "../../../../lib/ble-stack/health-ble-stack-L0.h"
#include <android/log.h>

//static jmethodID method_recvCallback;
//static jmethodID method_sendCallback;
static jmethodID method_sendData;

recv_cb_L0 recv_cb;
send_cb_L0 send_cb;

#define TAG "health-jni-L0"

#define ALOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
#define ALOGW(...)  __android_log_print(ANDROID_LOG_WARNNING,TAG,__VA_ARGS__)
#define ALOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__)

#define NELEM(x) ((int) (sizeof(x)/sizeof((x)[0])))

static JavaVM *vm = NULL;

static jobject sBlueToothObj;

health_ble_data_L0 g_data_L0;

jlong current_seq_id;

static char data_buf[256];

void checkAndClearException(JNIEnv* env,
                                               const char* methodName) {
    if (env->ExceptionCheck()) {
        ALOGE("An exception was thrown by callback '%s'.", methodName);
        env->ExceptionDescribe();
        env->ExceptionClear();
    }
}
static JNIEnv *getCurrentEnv() {
    JNIEnv* env;
    vm->GetEnv((void**)&env,JNI_VERSION_1_4);

    return env;
}

static int L0_send_data(char *data, size_t length, uint16_t seq_id) {
    ALOGV("L0_send_data seq_id %d \n",seq_id);
    jint ret = 0;
    current_seq_id = (jlong)seq_id;
    ALOGV("current_seq_id : %lld \n",current_seq_id);
    jbyteArray content = getCurrentEnv()->NewByteArray( (jsize)length);
    getCurrentEnv()->SetByteArrayRegion(content,0,(jsize)(length),(jbyte *)(data));
    ret = getCurrentEnv()->CallIntMethod(sBlueToothObj, method_sendData,(jlong)length,content);

    getCurrentEnv()->DeleteLocalRef(content);
    checkAndClearException(getCurrentEnv(), __FUNCTION__);

    return ret;
}

//发送数据的初始化
static void classInitNative(JNIEnv* env, jclass clazz) {
	ALOGV("classInitNative    \n");

	g_data_L0.data = data_buf;
    jclass sBlueToothObj =
        env->FindClass("com/baidu/wearable/ble/stack/HealthStackL0JNITransprot");

    method_sendData = env->GetMethodID(sBlueToothObj, "sendData", "(J[B)I");
}

static jint initNative(JNIEnv* env, jobject obj) {
    int ret;

    sBlueToothObj = env->NewGlobalRef(obj);

    env->GetJavaVM(&vm);

    ret = getL1CallBacks(&recv_cb,&send_cb,L0_send_data);
    
    ALOGV("initNative exit\n");
    
    return ret;
}


static int nativeSendReadResult(JNIEnv* env, jobject obj, jbyteArray content,jchar length) {
	int status_code = 1;
    
    ALOGV("nativeSendReadResult call\n");
	
    g_data_L0.length = length;
	env->GetByteArrayRegion(content, 0, length, (jbyte*)(g_data_L0.data));
	recv_cb(&g_data_L0,  status_code);
    
    ALOGV("nativeSendReadResult exit\n");

    return 0;
}

static int nativeSendWriteResult(JNIEnv* env, jobject obj, jint status_code) {
	ALOGV("BlueTooth: nativeSendWriteResult JNI current_seq_id: %lld \n ",current_seq_id);

	if(NULL != send_cb) {
		send_cb(current_seq_id,  status_code);
	} else {
		ALOGV("send_cb is NULL ");
	}

	return 0;
}

static JNINativeMethod sMethods[] = {
    /* name, signature, funcPtr */
    {"classInitNative", "()V", (void *) classInitNative},
    {"init", "()I", (void *) initNative},
    {"sendReadResult", "([BC)I", (void *) nativeSendReadResult},
    {"sendWriteResult", "(I)I", (void *) nativeSendWriteResult}
};

int register_com_baidu_wearable_bluetooth_proximity_JNITransprot(JNIEnv* env)
{
    jclass clazz;
    clazz = env->FindClass("com/baidu/wearable/ble/stack/HealthStackL0JNITransprot");
    return env->RegisterNatives( clazz,
                                    sMethods, NELEM(sMethods));
}
/*
 * JNI Initialization
 */
jint JNI_OnLoad_LO(JavaVM *jvm, void *reserved)
{
    JNIEnv *e;
    int status;

    ALOGV("health-jni-L0: loading JNI\n");

    // Check JNI version
    if (jvm->GetEnv((void **)&e, JNI_VERSION_1_6)) {
        ALOGE("JNI version mismatch error");
        return JNI_ERR;
    }

    if ((status = register_com_baidu_wearable_bluetooth_proximity_JNITransprot(e)) < 0) {
        ALOGE("jni BlueTooth registration failure, status: %d", status);
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}
