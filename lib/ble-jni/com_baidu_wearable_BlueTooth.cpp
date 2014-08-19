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
#include <assert.h>
#include <pthread.h>
#include "../../../../lib/ble-stack/health-ble-stack-L2.h"
#include <android/log.h>


static jmethodID method_sendCallback;
static jmethodID method_onReceiveSportData;
static jmethodID method_onReceiveSleepData;
static jmethodID method_onReceiveSleepSettingData;
static jmethodID method_onReceiveAlarmList;
static jmethodID method_onReceiveMoreSportData;
static jmethodID method_onReceiveSportDataSyncStart;
static jmethodID method_onReceiveSportDataSyncEnd;
static jmethodID method_onReceiveBindResponse;
static jmethodID method_onReceiveLoginResponse;

static jmethodID method_onReceiveTestModeEchoResponsee;
static jmethodID method_onReceiveTestModeChargeReadResponse;
static jmethodID method_onReceiveTestModeSnReadResponse;
static jmethodID method_onReceiveTestModeFlagReadResponse;
static jmethodID method_onReceiveTestModeSensorReadResponse;
static jmethodID method_onReceiveTestButton;
static jmethodID method_onReceiveOTAEnterOTAModeResponse;
static jmethodID method_resetBleConnect;

static jmethodID method_onReceiveRemoteControlCameraTakePicture;
static jmethodID method_onReceiveRemoteControlSingleClick;
static jmethodID method_onReceiveRemoteControlDoubleClick;



on_timer_fire_L2 f_on_timer_fire = NULL;

#define TAG "com_baidu_werable_BlueTooth"

#define ALOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
#define ALOGW(...)  __android_log_print(ANDROID_LOG_WARNNING,TAG,__VA_ARGS__)
#define ALOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__)

#define NELEM(x) ((int) (sizeof(x)/sizeof((x)[0])))

static JavaVM *vm = NULL;

static jobject sBlueToothObj;

int	timer_interval = 0;

static void checkAndClearExceptionFromCallback(JNIEnv* env,
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


static void on_receive_ota_command(L2_command_t *command) {
	struct list_head *p;

	if(NULL == command) {
	    	return;
	}

	list_for_each(p,&(command->key_list)) {
		L2_key_common_t *key_common = (L2_key_common_t*) p;
		L2_command_ota_enter_ota_mode_response_t *enter_ota_mode_response;
		switch (key_common->key) {
		case OTA_ENTER_OTA_MODE_RESPONSE:
			enter_ota_mode_response = (L2_command_ota_enter_ota_mode_response_t*) key_common;
			getCurrentEnv()->CallVoidMethod(
					sBlueToothObj,
					method_onReceiveOTAEnterOTAModeResponse,
					(jbyte)(enter_ota_mode_response->status_code),
					(jbyte)(enter_ota_mode_response->error_code));
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

			break;
		default:
			ALOGE("key not support:0x%x", key_common->key);
			assert(0);
			break;
		}
	}

}

static void on_receive_bind_command(L2_command_t *command) {
	struct list_head *p;

	if(NULL == command) {
	    	return;
	}

	list_for_each(p,&(command->key_list)) {
		L2_key_common_t *key_common = (L2_key_common_t*) p;
		L2_command_bind_bind_response_t *bind_response;
		L2_command_bind_login_response_t *login_response;
		switch (key_common->key) {
		case BIND_RESPONSE:
			bind_response = (L2_command_bind_bind_response_t*) key_common;
			getCurrentEnv()->CallVoidMethod(
					sBlueToothObj,
					method_onReceiveBindResponse,
					(jint)(bind_response->status_code));
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

			break;
		case LOGIN_RESPONSE:
			login_response = (L2_command_bind_login_response_t*) key_common;
			getCurrentEnv()->CallVoidMethod(
					sBlueToothObj,
					method_onReceiveLoginResponse,
					(jint)(login_response->status_code));
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

			break;
		default:
			ALOGE("key not support:0x%x", key_common->key);
			assert(0);
			break;
		}
	}

}

static void on_receive_sport_data_sport(L2_command_sport_data_response_sport_t *data_response_sport) {
	struct list_head *p;

	long day_second = 0;
	jclass cls_sportdata = NULL;
	jobject sportdata = NULL;

	jmethodID method_setDate = NULL;
	jmethodID method_getSecond = NULL;
	jmethodID method_addSection = NULL;

	jclass cls_sportdatasec = NULL;
	jobject sportdatasec = NULL;
	jmethodID method_setTimestamp = NULL;
	jmethodID method_setStep = NULL;
	jmethodID method_setCalory = NULL;
	jmethodID method_setDistance = NULL;

	ALOGV("on_receive_sport_data_sport");

	if (NULL == data_response_sport) {
		return;
	}

	cls_sportdata = getCurrentEnv()->FindClass(
			"com/baidu/wearable/ble/model/BlueToothSportData");
	if (NULL == cls_sportdata) {
		ALOGE("get cls_sportdata error");
		assert(0);
		return;
	}

	jmethodID cid = getCurrentEnv()->GetMethodID(cls_sportdata, "init", "()V");

	if (NULL == cid) {
		ALOGE("get cid method for  cls_sportdata error");
		assert(0);
		return;
	}

	sportdata = getCurrentEnv()->NewObject(cls_sportdata, cid);

	if (NULL == sportdata) {
		ALOGE("alloc sportdata error");
		assert(0);
		return;
	}


	method_setDate = getCurrentEnv()->GetMethodID(cls_sportdata, "setDate",
			"(III)V");
	method_getSecond = getCurrentEnv()->GetMethodID(cls_sportdata, "getSecond",
			"()J");
	method_addSection = getCurrentEnv()->GetMethodID(cls_sportdata,
			"addSection",
			"(Lcom/baidu/wearable/ble/model/BlueToothSportDataSection;)V");
	if (NULL == method_setDate || NULL == method_getSecond
			|| NULL == method_addSection) {
		ALOGE("get method for  BlueToothSportData error");
		assert(0);
		return;
	}
	ALOGV("year:%d month:%d day:%d",data_response_sport->year + 2000,data_response_sport->month,data_response_sport->day);

	getCurrentEnv()->CallVoidMethod(sportdata,
			method_setDate,
			(jint)(data_response_sport->year + 2000),
					(jint)(data_response_sport->month),
					(jint)(data_response_sport->day));
	checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

	day_second = getCurrentEnv()->CallLongMethod(
			sportdata, method_getSecond);
	checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

	cls_sportdatasec = getCurrentEnv()->FindClass(
					"com/baidu/wearable/ble/model/BlueToothSportDataSection");
	if (NULL == cls_sportdatasec) {
		ALOGE("get cls_sportdatasec error");
		assert(0);
		return;
	}

	method_setTimestamp = getCurrentEnv()->GetMethodID(cls_sportdatasec,
			"setTimestamp", "(JI)V");
	method_setStep = getCurrentEnv()->GetMethodID(cls_sportdatasec, "setStep",
			"(I)V");
	method_setCalory = getCurrentEnv()->GetMethodID(cls_sportdatasec,
			"setCalory", "(F)V");

	method_setDistance = getCurrentEnv()->GetMethodID(cls_sportdatasec,
			"setDistance", "(F)V");

	if (NULL == method_setTimestamp || NULL == method_setStep
			|| NULL == method_setCalory || NULL == method_setDistance) {
		ALOGE("get method for  BlueToothSportDataSection error");
		assert(0);
		return;
	}

	list_for_each(p,&(data_response_sport->sport_list)) {
		L2_sport_item_t *sport_item = (L2_sport_item_t*) p;

		sportdatasec = getCurrentEnv()->AllocObject(cls_sportdatasec);
		if (NULL == sportdatasec) {
			ALOGE("alloc sportdatasec error");
			assert(0);
			continue;
		}


		getCurrentEnv()->CallVoidMethod(
				sportdatasec,
				method_setTimestamp, (jlong) day_second,
				(jint)(sport_item->offset));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

		getCurrentEnv()->CallVoidMethod(
				sportdatasec, method_setStep,
				(jint)(sport_item->steps));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

		getCurrentEnv()->CallVoidMethod(
				sportdatasec, method_setCalory,
				(jfloat)(((float) sport_item->calory)) / 1000);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(
				sportdatasec, method_setDistance,
				(jfloat)(((float) sport_item->distance)) / 1000);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

		getCurrentEnv()->CallVoidMethod(
				sportdata, method_addSection,
				sportdatasec);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
	}

	getCurrentEnv()->CallVoidMethod(
			sBlueToothObj,
			method_onReceiveSportData, (jobject)(sportdata));
	checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

	return;

}

static void on_receive_sport_data_sleep(L2_command_sport_data_response_sleep_t *data_response_sleep) {
	struct list_head *p;
	long day_second = 0;
	jclass cls_sleepdata = NULL;
	jobject sleepdata = NULL;


	jmethodID method_setDate = NULL;
	jmethodID method_getSecond = NULL;
	jmethodID method_addSection = NULL;


	jclass cls_sleepdatasec = NULL;
	jobject sleepdatasec = NULL;
	jmethodID method_setMinute = NULL;
	jmethodID method_setType = NULL;

	if (NULL == data_response_sleep) {
		return;
	}

	cls_sleepdata = getCurrentEnv()->FindClass(
			"com/baidu/wearable/ble/model/BlueToothSleepData");
	if (NULL == cls_sleepdata) {
		ALOGE("get cls_sleepdata error");
		assert(0);
		return;
	}

	jmethodID cid = getCurrentEnv()->GetMethodID(cls_sleepdata, "init", "()V");

	if (NULL == cid) {
		ALOGE("get cid method for  cls_sleepdata error");
		assert(0);
		return;
	}

	sleepdata = getCurrentEnv()->NewObject(cls_sleepdata, cid);

	if (NULL == sleepdata) {
		ALOGE("alloc sleepdata error");
		assert(0);
		return;
	}

	method_setDate = getCurrentEnv()->GetMethodID(cls_sleepdata, "setDate",
			"(III)V");
	method_getSecond = getCurrentEnv()->GetMethodID(cls_sleepdata, "getSecond",
			"()J");
	method_addSection = getCurrentEnv()->GetMethodID(cls_sleepdata,
			"addSection",
			"(Lcom/baidu/wearable/ble/model/BlueToothSleepDataSection;)V");
	if (NULL == method_setDate || NULL == method_getSecond
			|| NULL == method_addSection) {
		ALOGE("get method for  BlueToothSleepData error");
		assert(0);
		return;
	}
	getCurrentEnv()->CallVoidMethod(sleepdata,
			method_setDate,
			(jint)(data_response_sleep->year + 2000),
					(jint)(data_response_sleep->month),
					(jint)(data_response_sleep->day));
	checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

	day_second = getCurrentEnv()->CallLongMethod(
			sleepdata, method_getSecond);
	checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

	cls_sleepdatasec = getCurrentEnv()->FindClass(
			"com/baidu/wearable/ble/model/BlueToothSleepDataSection");
	if (NULL == cls_sleepdatasec) {
		ALOGE("get cls_sleepdatasec error");
		assert(0);
		return;
	}

	method_setMinute = getCurrentEnv()->GetMethodID(cls_sleepdatasec,
			"setMinute", "(I)V");
	method_setType = getCurrentEnv()->GetMethodID(cls_sleepdatasec, "setType",
			"(I)V");
	if (NULL == method_setMinute || NULL == method_setType) {
		ALOGE("get method for  BlueToothSleepDataSection error");
		assert(0);
		return;
	}

	list_for_each(p,&(data_response_sleep->sleep_list)) {
		L2_sleep_item_t *sleep_item = (L2_sleep_item_t*) p;

		sleepdatasec = getCurrentEnv()->AllocObject(cls_sleepdatasec);
		if (NULL == sleepdatasec) {
			ALOGE("alloc sleepdatasec error");
			assert(0);
			continue;
		}


		getCurrentEnv()->CallVoidMethod(
				sleepdatasec, method_setMinute,
				(jint)(sleep_item->minute));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(
				sleepdatasec, method_setType,
				(jint)(sleep_item->mode));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

		getCurrentEnv()->CallVoidMethod(
				sleepdata, method_addSection,
				sleepdatasec);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
	}

	getCurrentEnv()->CallVoidMethod(
			sBlueToothObj,
			method_onReceiveSleepData, (jobject)(sleepdata));
	checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
}

static void on_receive_sport_data_sleep_setting(L2_command_sport_data_response_sleep_setting_t *data_response_sleep_setting) {
	struct list_head *p;
	long day_second = 0;
	jclass cls_sleepdata = NULL;
	jobject sleepdata = NULL;


	jmethodID method_setDate = NULL;
	jmethodID method_getSecond = NULL;
	jmethodID method_addSection = NULL;


	jclass cls_sleepdatasec = NULL;
	jobject sleepdatasec = NULL;
	jmethodID method_setMinute = NULL;
	jmethodID method_setType = NULL;

	if (NULL == data_response_sleep_setting) {
		return;
	}

	cls_sleepdata = getCurrentEnv()->FindClass(
			"com/baidu/wearable/ble/model/BlueToothSleepData");
	if (NULL == cls_sleepdata) {
		ALOGE("get cls_sleepdata error");
		assert(0);
		return;
	}

	jmethodID cid = getCurrentEnv()->GetMethodID(cls_sleepdata, "init", "()V");

	if (NULL == cid) {
		ALOGE("get cid method for  cls_sleepdata error");
		assert(0);
		return;
	}

	sleepdata = getCurrentEnv()->NewObject(cls_sleepdata, cid);

	if (NULL == sleepdata) {
		ALOGE("alloc sleepdata error");
		assert(0);
		return;
	}


	method_setDate = getCurrentEnv()->GetMethodID(cls_sleepdata, "setDate",
			"(III)V");
	method_getSecond = getCurrentEnv()->GetMethodID(cls_sleepdata, "getSecond",
			"()J");
	method_addSection = getCurrentEnv()->GetMethodID(cls_sleepdata,
			"addSection",
			"(Lcom/baidu/wearable/ble/model/BlueToothSleepDataSection;)V");
	if (NULL == method_setDate || NULL == method_getSecond
			|| NULL == method_addSection) {
		ALOGE("get method for  BlueToothSleepData error");
		assert(0);
		return;
	}
	getCurrentEnv()->CallVoidMethod(sleepdata,
			method_setDate,
			(jint)(data_response_sleep_setting->year + 2000),
					(jint)(data_response_sleep_setting->month),
					(jint)(data_response_sleep_setting->day));
	checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

	day_second = getCurrentEnv()->CallLongMethod(
			sleepdata, method_getSecond);
	checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);


	cls_sleepdatasec = getCurrentEnv()->FindClass(
			"com/baidu/wearable/ble/model/BlueToothSleepDataSection");
	if (NULL == cls_sleepdatasec) {
		ALOGE("get cls_sleepdatasec error");
		assert(0);
		return;
	}

	method_setMinute = getCurrentEnv()->GetMethodID(cls_sleepdatasec,
			"setMinute", "(I)V");
	method_setType = getCurrentEnv()->GetMethodID(cls_sleepdatasec, "setType",
			"(I)V");
	if (NULL == method_setMinute || NULL == method_setType) {
		ALOGE("get method for  BlueToothSleepDataSection error");
		assert(0);
		return;
	}

	list_for_each(p,&(data_response_sleep_setting->sleep_setting_list)) {
		L2_sleep_setting_item_t *sleep_setting_item = (L2_sleep_setting_item_t*) p;

		sleepdatasec = getCurrentEnv()->AllocObject(cls_sleepdatasec);
		if (NULL == sleepdatasec) {
			ALOGE("alloc sleepdatasec error");
			assert(0);
			continue;
		}

		getCurrentEnv()->CallVoidMethod(
				sleepdatasec, method_setMinute,
				(jint)(sleep_setting_item->minute));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(
				sleepdatasec, method_setType,
				(jint)(sleep_setting_item->mode));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

		getCurrentEnv()->CallVoidMethod(
				sleepdata, method_addSection,
				sleepdatasec);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
	}

	getCurrentEnv()->CallVoidMethod(
			sBlueToothObj,
			method_onReceiveSleepSettingData, (jobject)(sleepdata));
	checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
}

static void on_receive_sport_data_command(L2_command_t *command) {
	struct list_head *p;

	if (NULL == command) {
		return;
	}

	list_for_each(p,&(command->key_list)) {
		L2_key_common_t *key_common = (L2_key_common_t*) p;
		switch (key_common->key) {
		case DATA_RESPONSE_SPORT:
			on_receive_sport_data_sport(
					(L2_command_sport_data_response_sport_t*) key_common);

			break;
		case DATA_RESPONSE_SLEEP:
			ALOGV("DATA_RESPONSE_SLEEP");
			on_receive_sport_data_sleep(
					(L2_command_sport_data_response_sleep_t*) key_common);

			break;
		case DATA_RESPONSE_MORE:
			getCurrentEnv()->CallVoidMethod(
					sBlueToothObj,
					method_onReceiveMoreSportData);
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
			break;
		case DATA_RESPONSE_SLEEP_SETTING:
			ALOGV("DATA_RESPONSE_SLEEP_SETTING");
			on_receive_sport_data_sleep_setting(
								(L2_command_sport_data_response_sleep_setting_t*) key_common);
			break;
    case DATA_SYNC_START:
      getCurrentEnv()->CallVoidMethod(
                                        sBlueToothObj,
                                        method_onReceiveSportDataSyncStart);
      checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
      break;
    case DATA_SYNC_END:
      getCurrentEnv()->CallVoidMethod(
                                        sBlueToothObj,
                                        method_onReceiveSportDataSyncEnd);
      checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
      break;
		default:
			ALOGE("key not support:0x%x", key_common->key);
			assert(0);
			break;
		}
	}
}

static void on_receive_setting_get_alarm_response(L2_command_setting_get_alarm_response_t *get_alarm_response) {
	struct list_head *p;
	jclass cls_clocklist = NULL;
	jobject clocklist = NULL;

	jmethodID method_addClock = NULL;

	jclass cls_clock = NULL;
	jobject clock = NULL;
	jmethodID method_setAlarmId = NULL;
	jmethodID method_setYear = NULL;
	jmethodID method_setMonth = NULL;
	jmethodID method_setDay = NULL;
	jmethodID method_setHour = NULL;
	jmethodID method_setMinute = NULL;
	jmethodID method_setMon = NULL;
	jmethodID method_setTue = NULL;
	jmethodID method_setWed = NULL;
	jmethodID method_setThu = NULL;
	jmethodID method_setFri = NULL;
	jmethodID method_setSat = NULL;
	jmethodID method_setSun = NULL;

	if (NULL == get_alarm_response) {
		return;
	}

	cls_clocklist = getCurrentEnv()->FindClass(
			"com/baidu/wearable/ble/model/ClockList");
	if (NULL == cls_clocklist) {
		ALOGE("get cls_clocklist error");
		assert(0);
		return;
	}

	jmethodID cid = getCurrentEnv()->GetMethodID(cls_clocklist, "init", "()V");

	if (NULL == cid) {
		ALOGE("get cid method for  ClockList error");
		assert(0);
		return;
	}

	clocklist = getCurrentEnv()->NewObject(cls_clocklist,cid);

	if (NULL == clocklist) {
		ALOGE("alloc clocklist error");
		assert(0);
		return;
	}

	method_addClock = getCurrentEnv()->GetMethodID(cls_clocklist, "addClock",
			"(Lcom/baidu/wearable/ble/model/Clock;)V");
	if (NULL == method_addClock) {
		ALOGE("get method for  ClockList error");
		assert(0);
		getCurrentEnv()->DeleteLocalRef(clocklist);
		return;
	}

	cls_clock = getCurrentEnv()->FindClass("com/baidu/wearable/ble/model/Clock");
	if (NULL == cls_clock) {
		ALOGE("get cls_clock error");
		assert(0);
		return;
	}

	method_setAlarmId = getCurrentEnv()->GetMethodID(cls_clock, "setAlarmId",
			"(I)V");
	method_setYear = getCurrentEnv()->GetMethodID(cls_clock, "setYear", "(I)V");
	method_setMonth = getCurrentEnv()->GetMethodID(cls_clock, "setMonth",
			"(I)V");
	method_setDay = getCurrentEnv()->GetMethodID(cls_clock, "setDay", "(I)V");
	method_setHour = getCurrentEnv()->GetMethodID(cls_clock, "setHour", "(I)V");
	method_setMinute = getCurrentEnv()->GetMethodID(cls_clock, "setMinute",
			"(I)V");
	method_setMon = getCurrentEnv()->GetMethodID(cls_clock, "setMon", "(Z)V");
	method_setTue = getCurrentEnv()->GetMethodID(cls_clock, "setTue", "(Z)V");
	method_setWed = getCurrentEnv()->GetMethodID(cls_clock, "setWed", "(Z)V");
	method_setThu = getCurrentEnv()->GetMethodID(cls_clock, "setThu", "(Z)V");
	method_setFri = getCurrentEnv()->GetMethodID(cls_clock, "setFri", "(Z)V");
	method_setSat = getCurrentEnv()->GetMethodID(cls_clock, "setSat", "(Z)V");
	method_setSun = getCurrentEnv()->GetMethodID(cls_clock, "setSun", "(Z)V");
	if (NULL == method_setAlarmId || NULL == method_setYear
			|| NULL == method_setMonth || NULL == method_setDay
			|| NULL == method_setHour || NULL == method_setMinute
			|| NULL == method_setMon || NULL == method_setTue
			|| NULL == method_setWed || NULL == method_setThu
			|| NULL == method_setFri || NULL == method_setSat
			|| NULL == method_setSun) {
		ALOGE("get method for  Clock error");
		assert(0);
		return;
	}


	list_for_each(p,&(get_alarm_response->alarm_list)) {
		L2_command_setting_alarm_item_t *alarm_item =
				(L2_command_setting_alarm_item_t*) p;

		clock = getCurrentEnv()->AllocObject(cls_clock);
		if (NULL == clock) {
			ALOGE("alloc clock error");
			assert(0);
			continue;
		}


		getCurrentEnv()->CallVoidMethod(clock,
				method_setAlarmId, (jint)(alarm_item->alarm_id));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

		getCurrentEnv()->CallVoidMethod(clock,
				method_setYear, (jint)(alarm_item->year + 2000));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(clock,
				method_setMonth, (jint)(alarm_item->month));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(clock,
				method_setDay, (jint)(alarm_item->day));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

		getCurrentEnv()->CallVoidMethod(clock,
				method_setHour, (jint)(alarm_item->hour));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(clock,
				method_setMinute, (jint)(alarm_item->minute));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

		getCurrentEnv()->CallVoidMethod(clock,
				method_setMon, (jint)(alarm_item->Mon));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(clock,
				method_setTue, (jint)(alarm_item->Tue));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

		getCurrentEnv()->CallVoidMethod(clock,
				method_setWed, (jint)(alarm_item->Wed));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(clock,
				method_setThu, (jint)(alarm_item->Thu));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(clock,
				method_setFri, (jint)(alarm_item->Fri));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(clock,
				method_setSat, (jint)(alarm_item->Sat));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		getCurrentEnv()->CallVoidMethod(clock,
				method_setSun, (jint)(alarm_item->Sun));
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

		getCurrentEnv()->CallVoidMethod(
				clocklist, method_addClock,
				clock);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
	}

	getCurrentEnv()->CallVoidMethod(
			sBlueToothObj,
			method_onReceiveAlarmList, (jobject)(clocklist));
	checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

}

static void on_receive_setting_command(L2_command_t *command) {

	struct list_head *p;

	if (NULL == command) {
		return;
	}


	list_for_each(p,&(command->key_list)) {
		L2_key_common_t *key_common = (L2_key_common_t*) p;
		L2_command_setting_get_alarm_response_t *get_alarm_response;
		switch (key_common->key) {
		case SETTING_GET_ALARM_RESPONSE:
			on_receive_setting_get_alarm_response(
					(L2_command_setting_get_alarm_response_t*) key_common);
			break;
		default:
			ALOGE("key not support:0x%x", key_common->key);
			assert(0);
			break;
		}
	}


}

static void on_receive_test_mode_command(L2_command_t *command) {
	struct list_head *p;
	L2_command_test_echo_response_t *echo_response;
	L2_command_test_charge_response_t *charge_response;
	L2_command_test_sn_read_response_t *sn_read_response;
	L2_command_test_flag_read_response_t *flag_read_response;
	L2_command_test_sensor_read_response_t *sensor_read_response;
	L2_command_test_button_t *test_button;

	jbyteArray bytearr = NULL;

    ALOGV("on_receive_test_mode_command");
	if (NULL == command) {
		return;
	}

	list_for_each(p,&(command->key_list)) {
		L2_key_common_t *key_common = (L2_key_common_t*) p;
		switch (key_common->key) {
		case ECHO_RESPONSE:
			echo_response = (L2_command_test_echo_response_t*)p;
			bytearr = getCurrentEnv()->NewByteArray(echo_response->length);
			if(NULL == bytearr) {
				ALOGE("alloc bytearr error");
				return;
			}
			getCurrentEnv()->SetByteArrayRegion(bytearr,0,echo_response->length,(jbyte*)(echo_response->data));
			getCurrentEnv()->CallVoidMethod(sBlueToothObj,
					method_onReceiveTestModeEchoResponsee, bytearr);
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
			getCurrentEnv()->DeleteLocalRef(bytearr);
			break;
		case CHARGE_RESPONSE:
			charge_response = (L2_command_test_charge_response_t*)p;
			getCurrentEnv()->CallVoidMethod(
					sBlueToothObj,
					method_onReceiveTestModeChargeReadResponse, charge_response->voltage);
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

			break;
		case SN_RESPONSE:
			sn_read_response = (L2_command_test_sn_read_response_t*)p;
			bytearr = getCurrentEnv()->NewByteArray(sizeof(sn_read_response->sn));
			if (NULL == bytearr) {
				ALOGE("alloc bytearr error");
				return;
			}
			getCurrentEnv()->SetByteArrayRegion(bytearr,0,sizeof(sn_read_response->sn),(jbyte*)(sn_read_response->sn));
			getCurrentEnv()->CallVoidMethod(sBlueToothObj,
					method_onReceiveTestModeSnReadResponse, bytearr);
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
			getCurrentEnv()->DeleteLocalRef(bytearr);

			break;
		case TEST_FLAG_RESPONSE:
			flag_read_response = (L2_command_test_flag_read_response_t*)p;
			getCurrentEnv()->CallVoidMethod(
					sBlueToothObj,
					method_onReceiveTestModeFlagReadResponse,
					flag_read_response->test_flag);
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);


			break;
		case SENSOR_DATA_RESPONSE:
			sensor_read_response = (L2_command_test_sensor_read_response_t*)p;
			getCurrentEnv()->CallVoidMethod(
								sBlueToothObj,
								method_onReceiveTestModeSensorReadResponse,
								sensor_read_response->x_axis,sensor_read_response->y_axis,sensor_read_response->z_axis);
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

			break;
		case BUTTON_TEST:
			test_button = (L2_command_test_button_t*)p;
			getCurrentEnv()->CallVoidMethod(
								sBlueToothObj,
								method_onReceiveTestButton,
								test_button->code,test_button->button_id,test_button->timestamp);
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);

			break;
		default:
			ALOGE("key not support:0x%x", key_common->key);
			assert(0);
			break;
		}
	}
}


static void on_receive_remote_control_command(L2_command_t *command) {
	struct list_head *p;
	L2_command_remote_control_camera_take_picture_t *camera_take_picture;

    ALOGV("on_receive_remote_control_command");
	if (NULL == command) {
		return;
	}

	list_for_each(p,&(command->key_list)) {
		L2_key_common_t *key_common = (L2_key_common_t*) p;
		switch (key_common->key) {
		case CAMERA_TAKE_PICTURE:
			getCurrentEnv()->CallVoidMethod(
								sBlueToothObj,
								method_onReceiveRemoteControlCameraTakePicture);
			checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
			break;
		case SINGLE_CLICK:
					getCurrentEnv()->CallVoidMethod(
										sBlueToothObj,
										method_onReceiveRemoteControlSingleClick);
					checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
					break;
		case DOUBLE_CLICK:
					getCurrentEnv()->CallVoidMethod(
										sBlueToothObj,
										method_onReceiveRemoteControlDoubleClick);
					checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
					break;
		default:
			ALOGE("key not support:0x%x", key_common->key);
			assert(0);
			break;
		}
	}
}

static void L2_recv_callback(L2_command_t *command) {


    ALOGV("L2_recv_callback call");

    if(NULL == command) {
    	return;
    }

    switch(command->header.command_id) {
		case HEALTH_BLE_COMMAND_ROM_UPATE:
			on_receive_ota_command(command);
			break;
    	case HEALTH_BLE_COMMAND_BIND:
    		on_receive_bind_command(command);
    		break;
    	case HEALTH_BLE_COMMAND_SETTING:
    		on_receive_setting_command(command);
    		break;
    	case HEALTH_BLE_COMMAND_SPORT_DATA:
    		on_receive_sport_data_command(command);
    	    break;
    	case HEALTH_BLE_COMMAND_FACTORY_TEST:
    		on_receive_test_mode_command(command);
    		break;
    	case HEALTH_BLE_COMMAND_REMOTE_CONTROL:
			on_receive_remote_control_command(command);
			break;
    	default:
    		ALOGE("command_id not support:0x%x", command->header.command_id);
    		assert(0);
		break;
    }


    ALOGV("L2_recv_callback exit");
}

static void L2_send_callback(unsigned short seq_id, int status_code) {

    getCurrentEnv()->CallVoidMethod(sBlueToothObj, method_sendCallback, (jint)status_code,(jlong)seq_id);

    checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
}

static void need_reset_l2() {

    getCurrentEnv()->CallVoidMethod(sBlueToothObj, method_resetBleConnect);

    checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
}

static void L2_create_timer(on_timer_fire_L2 timer_fire,int second) {
	f_on_timer_fire = timer_fire;

	assert(1 == second);

	timer_interval = second;
}

static void L2_stop_timer() {
	f_on_timer_fire = NULL;

	timer_interval = 0;
}

static jint sendCommandWithEmptyValueKey(unsigned char command_id,unsigned char command_version,unsigned char key) {
	int ret;

	ALOGV("sendCommandWithEmptyValueKey command_id:0x%x key:0x%x ",command_id,key);

	L2_command_t *command = (L2_command_t*) malloc(sizeof(L2_command_t));
	if (NULL == command) {
		ALOGE("malloc command error ");
		return -1;
	}
	memset(command, 0, sizeof(L2_command_t));

	INIT_LIST_HEAD(&(command->key_list));

	command->header.command_id = command_id;
	command->header.command_version = command_version;

	L2_key_common_t *key_common =
			(L2_key_common_t*) malloc(
					sizeof(L2_key_common_t));
	if (NULL == key_common) {
		ALOGE("malloc key_common error ");

		free_command(command);
		return -1;
	}
	memset(key_common,0,sizeof(L2_key_common_t));

	key_common->key = key;

	list_add_tail(&(key_common->list), &(command->key_list));

	ret = send_L2(command);

	free_command(command);

	return ret;
}

static jint sendCommandWithKey(unsigned char command_id,unsigned char command_version,L2_key_common_t *key_common) {
	int ret;

	ALOGV("sendCommandWithKey command_id:0x%x key:0x%x ", command_id, key_common->key);

	L2_command_t *command = (L2_command_t*) malloc(sizeof(L2_command_t));
	if (NULL == command) {
		ALOGE("malloc command error ");
		free_key_common(command_id,key_common);
		return -1;
	}
	memset(command, 0, sizeof(L2_command_t));

	INIT_LIST_HEAD(&(command->key_list));

	command->header.command_id = command_id;
	command->header.command_version = command_version;

	list_add_tail(&(key_common->list), &(command->key_list));

	ret = send_L2(command);

	free_command(command);

	return ret;

}


jint otaEnterOTAModeNative(JNIEnv* env, jobject obj) {
	ALOGV("otaEnterOTAModeNative ");

	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_ROM_UPATE,0,OTA_ENTER_OTA_MODE);
}

jint bindNative(JNIEnv* env, jobject obj, jbyteArray userId) {
	int ret;

	ALOGV("bindNative ");

	unsigned short length = env->GetArrayLength(userId);

	if(length > 32) {
		ALOGE("bindNative userid too long, length:%d", length);
		return -1;
	}

	L2_command_bind_bind_request_t *bind_request  = (L2_command_bind_bind_request_t*)malloc(sizeof(L2_command_bind_bind_request_t));
	if(NULL == bind_request) {
		ALOGE("malloc bind_request error ");
		return -1;
	}

	bind_request->common.key = BIND_REQUEST;
	memset(bind_request->userid,0,32);
	env->GetByteArrayRegion(userId,0,length,(jbyte*)(bind_request->userid));

	return sendCommandWithKey(HEALTH_BLE_COMMAND_BIND,0,(L2_key_common_t*)bind_request);
}

jint loginNative(JNIEnv* env, jobject obj, jbyteArray userId) {
		int ret;

		ALOGV("loginNative ");

		unsigned short length = env->GetArrayLength(userId);

		if (length > 32) {
			ALOGE("loginNative userid too long, length:%d", length);
			return -1;
		}

		L2_command_bind_login_request_t *login_request =
				(L2_command_bind_login_request_t*) malloc(
						sizeof(L2_command_bind_login_request_t));
		if (NULL == login_request) {
			ALOGE("malloc login_request error ");
			return -1;
		}

		login_request->common.key = LOGIN_REQUEST;
		memset(login_request->userid, 0, 32);
		env->GetByteArrayRegion(userId, 0, length, (jbyte*)(login_request->userid));

		return sendCommandWithKey(HEALTH_BLE_COMMAND_BIND,0,(L2_key_common_t*)login_request);

}

jint setTimeNative(JNIEnv* env, jobject obj, jint year,jint month,jint day,jint hour,jint minute,jint second) {
	int ret;

	ALOGV("setTimeNative ");

	L2_command_setting_time_t *setting_time =
			(L2_command_setting_time_t*) malloc(
					sizeof(L2_command_setting_time_t));
	if (NULL == setting_time) {
		ALOGE("malloc setting_time error ");
		return -1;
	}

	setting_time->common.key = SETTING_TIME;

	setting_time->year = year - 2000;
	setting_time->month = month;
	setting_time->day = day;
	setting_time->hour = hour;
	setting_time->minute = minute;
	setting_time->second = second;

	return sendCommandWithKey(HEALTH_BLE_COMMAND_SETTING,0,(L2_key_common_t*)setting_time);
}

jint setAlarmListNative(JNIEnv* env, jobject obj, jobject alarmList) {
	int ret;
	int size;
	int i;

	ALOGV("setAlarmListNative ");

	jclass cls_clocklist = env->FindClass("com/baidu/wearable/ble/model/ClockList");
	if (NULL == cls_clocklist) {
		ALOGE("get cls_clocklist error");
		return -1;
	}

	jmethodID method_getListSize = env->GetMethodID(cls_clocklist,
			"getListSize", "()I");
	jmethodID method_getClock = env->GetMethodID(cls_clocklist,
			"getClock", "(I)Lcom/baidu/wearable/ble/model/Clock;");
	if (NULL == method_getListSize || NULL == method_getClock) {
		ALOGE("get method for  /ClockList error");
		return -1;
	}

	jclass cls_clock = env->FindClass("com/baidu/wearable/ble/model/Clock");
	if (NULL == cls_clock) {
		ALOGE("get cls_clock error");
		return -1;
	}
	jmethodID method_getAlarmId = env->GetMethodID(cls_clock,
			"getAlarmId", "()I");
	jmethodID method_getYear = env->GetMethodID(cls_clock,
			"getYear", "()I");
	jmethodID method_getMonth = env->GetMethodID(cls_clock,
			"getMonth", "()I");
	jmethodID method_getDay = env->GetMethodID(cls_clock,
			"getDay", "()I");
	jmethodID method_getHour = env->GetMethodID(cls_clock,
			"getHour", "()I");
	jmethodID method_getMinute = env->GetMethodID(cls_clock,
			"getMinute", "()I");
	jmethodID method_isMon = env->GetMethodID(cls_clock,
				"isMon", "()Z");
	jmethodID method_isTue = env->GetMethodID(cls_clock,
					"isTue", "()Z");
	jmethodID method_isWed = env->GetMethodID(cls_clock,
						"isWed", "()Z");
	jmethodID method_isThu = env->GetMethodID(cls_clock,
							"isThu", "()Z");
	jmethodID method_isFri = env->GetMethodID(cls_clock,
							"isFri", "()Z");
	jmethodID method_isSat = env->GetMethodID(cls_clock,
							"isSat", "()Z");
	jmethodID method_isSun = env->GetMethodID(cls_clock,
							"isSun", "()Z");

	if (NULL == method_getAlarmId || NULL == method_getYear || NULL == method_getMonth
			|| NULL == method_getDay || NULL == method_getHour
			|| NULL == method_getMinute || NULL == method_isMon
			|| NULL == method_isTue || NULL ==  method_isFri
			|| NULL == method_isSat || NULL == method_isSun) {
		ALOGE("get method for  L2Alarm error");
		return -1;
	}

	size = env->CallIntMethod(alarmList,
			method_getListSize);

	if(size > 8) {
		ALOGE("alarm list size error");
		return -1;
	}


	L2_command_setting_alarm_t *setting_alarm =
					(L2_command_setting_alarm_t*) malloc(sizeof(L2_command_setting_alarm_t));
	if (NULL == setting_alarm) {
		ALOGE("malloc setting_alarm error ");
		return -1;
	}

	memset(setting_alarm, 0, sizeof(L2_command_setting_alarm_t));
	setting_alarm->common.key = SETTING_ALARM;
	INIT_LIST_HEAD(&(setting_alarm->alarm_list));

	for(i=0; i<size; i++) {
		int alarm_id;
		int year;
		int month;
		int day;
		int hour;
		int minute;
		bool isMon;
		bool isTue;
		bool isWed;
		bool isThu;
		bool isFri;
		bool isSat;
		bool isSun;

		jobject clock = env->CallObjectMethod(alarmList,method_getClock,i);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		if(NULL == clock) {
			ALOGE("get clock NULL for i:%d ",i);
			continue;
		}

		alarm_id = env->CallIntMethod(clock,method_getAlarmId);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		year = env->CallIntMethod(clock,method_getYear);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		month = env->CallIntMethod(clock,method_getMonth);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		day = env->CallIntMethod(clock,method_getDay);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		hour = env->CallIntMethod(clock,method_getHour);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		minute = env->CallIntMethod(clock,method_getMinute);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		isMon = env->CallBooleanMethod(clock,method_isMon);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		isTue = env->CallBooleanMethod(clock,method_isTue);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		isWed = env->CallBooleanMethod(clock,method_isWed);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		isThu = env->CallBooleanMethod(clock,method_isThu);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		isFri = env->CallBooleanMethod(clock,method_isFri);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		isSat = env->CallBooleanMethod(clock,method_isSat);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		isSun = env->CallBooleanMethod(clock,method_isSun);
		checkAndClearExceptionFromCallback(getCurrentEnv(), __FUNCTION__);
		ALOGE("jni alarm_id:%d,year:%d,month:%d,day:%d,hour:%d,minute:%d,isMon:%d,isTue:%d,isWed:%d,isThu:%d,isFri:%d,isSat:%d,isSun:%d",alarm_id,year,month,day,hour,minute,isMon,isTue,isWed,isThu,isFri,isSat,isSun);

		L2_command_setting_alarm_item_t *alarm_item =
							(L2_command_setting_alarm_item_t*) malloc(sizeof(L2_command_setting_alarm_item_t));
			if (NULL == alarm_item) {
				ALOGE("malloc alarm_item error ");
				free_key_common(HEALTH_BLE_COMMAND_SETTING,(L2_key_common_t*)setting_alarm);
				return -1;
			}

			memset(alarm_item, 0, sizeof(L2_command_setting_alarm_item_t));

			alarm_item->alarm_id = alarm_id;
			alarm_item->year = year - 2000;
			alarm_item->month = month;
			alarm_item->day = day;
			alarm_item->hour = hour;
			alarm_item->minute = minute;

			alarm_item->Mon = isMon;
			alarm_item->Tue = isTue;
			alarm_item->Wed = isWed;
			alarm_item->Thu = isThu;
			alarm_item->Fri = isFri;
			alarm_item->Sat = isSat;
			alarm_item->Sun = isSun;

			list_add_tail(&(alarm_item->list), &(setting_alarm->alarm_list));
	}

	return sendCommandWithKey(HEALTH_BLE_COMMAND_SETTING,0,(L2_key_common_t*)setting_alarm);
}

jint getAlarmListNative(JNIEnv* env, jobject obj) {
	ALOGV("getAlarmListNative ");

	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_SETTING,0,SETTING_GET_ALARM_REQUEST);
}

jint setSportTargetNative(JNIEnv* env, jobject obj, jint sport_target) {
	int ret;

	ALOGV("setSportTargetNative ");

	L2_command_setting_sport_target_t *setting_sport_target =
			(L2_command_setting_sport_target_t*) malloc(
					sizeof(L2_command_setting_sport_target_t));
	if (NULL == setting_sport_target) {
		ALOGE("malloc setting_sport_target error ");
		return -1;
	}

	setting_sport_target->common.key = SETTING_SPORT_TARGET;

	setting_sport_target->target = sport_target;


	return sendCommandWithKey(HEALTH_BLE_COMMAND_SETTING,0,(L2_key_common_t*)setting_sport_target);
}

jint setUserProfileNative(JNIEnv* env, jobject obj, bool isMale,jint age,jint height,jint weight) {
	int ret;

	ALOGV("setUserProfileNative ");

	L2_command_setting_user_profile_t *setting_user_profile =
			(L2_command_setting_user_profile_t*) malloc(
					sizeof(L2_command_setting_user_profile_t));
	if (NULL == setting_user_profile) {
		ALOGE("malloc setting_user_profile error ");
		return -1;
	}

	setting_user_profile->common.key = SETTING_USER_PROFILE;

	setting_user_profile->ismale = isMale;
	setting_user_profile->age = age;
	setting_user_profile->height = height;
	setting_user_profile->weight  = weight;


	return sendCommandWithKey(HEALTH_BLE_COMMAND_SETTING,0,(L2_key_common_t*)setting_user_profile);
}

jint setLinklostNative(JNIEnv* env, jobject obj, jint alert_level) {
	int ret;

	ALOGV("setLinklostNative ");

	L2_command_setting_link_lost_request_t *setting_link_lost_request =
			(L2_command_setting_link_lost_request_t*) malloc(
					sizeof(L2_command_setting_link_lost_request_t));
	if (NULL == setting_link_lost_request) {
		ALOGE("malloc setting_link_lost_request error ");
		return -1;
	}

	setting_link_lost_request->common.key = SETTING_LINK_LOST_REQUEST;

	setting_link_lost_request->alert_level = alert_level;


	return sendCommandWithKey(HEALTH_BLE_COMMAND_SETTING,0,(L2_key_common_t*)setting_link_lost_request);
}

jint setStillAlarmNative(JNIEnv* env, jobject obj, jint enable,jint steps,jint minutes,jint start_hour,jint end_hour,jboolean mon,
		jboolean tue,jboolean wed,jboolean thu,jboolean fri,jboolean sat,jboolean sun) {
	int ret;

	ALOGV("setStillAlarmNative ");

	L2_command_setting_still_alarm_request_t *setting_still_alarm_request =
			(L2_command_setting_still_alarm_request_t*) malloc(
					sizeof(L2_command_setting_still_alarm_request_t));
	if (NULL == setting_still_alarm_request) {
		ALOGE("malloc setting_still_alarm_request error ");
		return -1;
	}

	setting_still_alarm_request->common.key = SETTING_STILL_ALARM;

	setting_still_alarm_request->enable = enable;
	setting_still_alarm_request->steps = steps;
	setting_still_alarm_request->minutes = minutes;
	setting_still_alarm_request->start_hour = start_hour;
	setting_still_alarm_request->end_hour = end_hour;
	setting_still_alarm_request->day_flag = ((mon?1:0) << 6) | ((tue?1:0) << 5) | ((wed?1:0) << 4)
			| ((thu?1:0) << 3) | ((fri?1:0) << 2) | ((sat?1:0) << 1) | ((sun?1:0));


	return sendCommandWithKey(HEALTH_BLE_COMMAND_SETTING,0,(L2_key_common_t*)setting_still_alarm_request);
}


static jint setOperationSystemNative(JNIEnv* env, jobject obj, jint os, jint reserved) {
	ALOGV("setOperationSystemNative ");

    L2_command_setting_os_t *setting_os =
            (L2_command_setting_os_t*)malloc(sizeof(L2_command_setting_os_t));
    if(NULL==setting_os){
		ALOGE("malloc setting_os error ");
		return -1;
    }

    memset(setting_os, 0, sizeof(setting_os));

    setting_os->common.key = SETTING_OS;

    if(os == SETTING_OS_IOS){
        setting_os->value = SETTING_OS_IOS;
    }else if(os == SETTING_OS_ANDROID){
        setting_os->value = SETTING_OS_ANDROID;
    }else{
        setting_os->value = SETTING_OS_UNKNOW;
    }
    setting_os->reserved = reserved;

	return sendCommandWithKey(HEALTH_BLE_COMMAND_SETTING, 0,
			(L2_key_common_t*)setting_os);
}

static jint setLeftOrRightHandNative(JNIEnv* env, jobject obj,jboolean isLeft) {
	int ret;

	ALOGV("setLeftOrRightNative ");

	L2_command_setting_left_or_right_hand_t *setting_left_or_right =
			(L2_command_setting_left_or_right_hand_t*) malloc(
					sizeof(L2_command_setting_left_or_right_hand_t));
	if (NULL == setting_left_or_right) {
		ALOGE("malloc setting_left_or_right error ");
		return -1;
	}
	memset(setting_left_or_right, 0,
			sizeof(L2_command_setting_left_or_right_hand_t));

	setting_left_or_right->common.key = SETTING_LEFT_OR_RIGHT;
	
	if(true == isLeft) {
		setting_left_or_right->value = SETTING_LEFT_HAND;
	} else {
		setting_left_or_right->value = SETTING_RIGHT_HAND;
	}

	return sendCommandWithKey(HEALTH_BLE_COMMAND_SETTING, 0,
			(L2_key_common_t*)setting_left_or_right);
}

jint phoneCommingNative(JNIEnv* env, jobject obj) {

	ALOGV("phoneCommingNative ");

	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_ALARM,0,PHONE_COMMING);
}

jint phoneAnswerNative(JNIEnv* env, jobject obj) {

	ALOGV("phoneAnswerNative ");
	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_ALARM,0,PHONE_ANSWER);
}

jint phoneDenyNative(JNIEnv* env, jobject obj) {

	ALOGV("phoneDenyNative ");
	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_ALARM,0,PHONE_DENY);
}



jint requestDataNative(JNIEnv* env, jobject obj) {
	ALOGV("requestDataNative ");
	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_SPORT_DATA,0,DATA_REQUEST);
}

jint setDataSyncNative(JNIEnv* env, jobject obj, jint enable) {
	int ret;

		ALOGV("setDataSyncNative ");

		L2_command_sport_data_sync_setting_t *sport_data_sync_setting =
				(L2_command_sport_data_sync_setting_t*) malloc(
						sizeof(L2_command_sport_data_sync_setting_t));
		if (NULL == sport_data_sync_setting) {
			ALOGE("malloc sport_data_sync_setting error ");
			return -1;
		}

		sport_data_sync_setting->common.key = DATA_SYNC_SETTING;

		sport_data_sync_setting->enable = enable;


		return sendCommandWithKey(HEALTH_BLE_COMMAND_SPORT_DATA,0,(L2_key_common_t*)sport_data_sync_setting);
}


jint setDailySportDataNative(JNIEnv* env, jobject obj, jint dailyStep,jint dailyDistance,jint dailyCalory) {
	int ret;

		ALOGV("setDailySportDataNative ");

		L2_command_sport_daily_data_sync_t *daily_data_sync =
				(L2_command_sport_daily_data_sync_t*) malloc(
						sizeof(L2_command_sport_daily_data_sync_t));
		if (NULL == daily_data_sync) {
			ALOGE("malloc daily_data_sync error ");
			return -1;
		}

		daily_data_sync->common.key = DATA_DAILY_DATA_SYNC;

		daily_data_sync->daily_step = dailyStep;
		
		daily_data_sync->daily_distance = dailyDistance;
		
		daily_data_sync->daily_calory = dailyCalory;


		return sendCommandWithKey(HEALTH_BLE_COMMAND_SPORT_DATA,0,(L2_key_common_t*)daily_data_sync);
}

void bleStackTimeFireNative() {

	if(NULL != f_on_timer_fire) {
		f_on_timer_fire();
	}
}


static void classInitNative(JNIEnv* env, jclass clazz) {

    ALOGV(" classInitNative\n");

    jclass class_BlueTooth =
        env->FindClass("com/baidu/wearable/ble/stack/BlueTooth");

    if(NULL == class_BlueTooth) {
    	ALOGE(" classInitNative get com/baidu/wearable/ble/stack/BlueTooth NULL\n");
    	return;
    }

    method_sendCallback = env->GetMethodID(class_BlueTooth, "onSendCallback", "(IJ)V");
    method_onReceiveSportData = env->GetMethodID(class_BlueTooth, "onReceiveSportData", "(Lcom/baidu/wearable/ble/model/BlueToothSportData;)V");
    method_onReceiveSleepData = env->GetMethodID(class_BlueTooth, "onReceiveSleepData", "(Lcom/baidu/wearable/ble/model/BlueToothSleepData;)V");
    method_onReceiveSleepSettingData = env->GetMethodID(class_BlueTooth, "onReceiveSleepSettingData", "(Lcom/baidu/wearable/ble/model/BlueToothSleepData;)V");
    method_onReceiveAlarmList = env->GetMethodID(class_BlueTooth, "onReceiveAlarmList", "(Lcom/baidu/wearable/ble/model/ClockList;)V");
    method_onReceiveMoreSportData = env->GetMethodID(class_BlueTooth, "onReceiveMoreSportData", "()V");
    method_onReceiveSportDataSyncStart = env->GetMethodID(class_BlueTooth, "onReceiveSportDataSyncStart", "()V");
    method_onReceiveSportDataSyncEnd = env->GetMethodID(class_BlueTooth, "onReceiveSportDataSyncEnd", "()V");
    method_onReceiveBindResponse = env->GetMethodID(class_BlueTooth, "onReceiveBindResponse", "(I)V");
    method_onReceiveLoginResponse = env->GetMethodID(class_BlueTooth, "onReceiveLoginResponse", "(I)V");

    method_onReceiveTestModeEchoResponsee = env->GetMethodID(class_BlueTooth, "onReceiveTestModeEchoResponse", "([B)V");
    method_onReceiveTestModeChargeReadResponse = env->GetMethodID(class_BlueTooth, "onReceiveTestModeChargeReadResponse", "(S)V");
    method_onReceiveTestModeSnReadResponse = env->GetMethodID(class_BlueTooth, "onReceiveTestModeSnReadResponse", "([B)V");
    method_onReceiveTestModeFlagReadResponse = env->GetMethodID(class_BlueTooth, "onReceiveTestModeFlagReadResponse", "(B)V");
    method_onReceiveTestModeSensorReadResponse = env->GetMethodID(class_BlueTooth, "onReceiveTestModeSensorReadResponse", "(SSS)V");
    method_onReceiveTestButton = env->GetMethodID(class_BlueTooth, "onReceiveTestButton", "(IIJ)V");

    method_onReceiveOTAEnterOTAModeResponse = env->GetMethodID(class_BlueTooth, "onReceiveOTAEnterOTAModeResponse", "(BB)V");

    method_resetBleConnect = env->GetMethodID(class_BlueTooth, "resetBleConnect", "()V");

    method_onReceiveRemoteControlCameraTakePicture = env->GetMethodID(class_BlueTooth, "onReceiveRemoteControlCameraTakePicture", "()V");

    method_onReceiveRemoteControlSingleClick= env->GetMethodID(class_BlueTooth, "onReceiveRemoteControlSingleClick", "()V");

    method_onReceiveRemoteControlDoubleClick = env->GetMethodID(class_BlueTooth, "onReceiveRemoteControlDoubleClick", "()V");

    if(NULL == method_sendCallback || NULL == method_onReceiveSportData
    		|| NULL == method_onReceiveSleepData || NULL == method_onReceiveMoreSportData || NULL == method_onReceiveSportDataSyncStart
        || NULL == method_onReceiveSportDataSyncEnd
        || NULL == method_onReceiveSleepSettingData
    		|| NULL == method_onReceiveBindResponse || NULL == method_onReceiveLoginResponse || NULL == method_onReceiveAlarmList
    		|| NULL == method_onReceiveTestModeEchoResponsee || NULL == method_onReceiveTestModeChargeReadResponse
    		|| NULL == method_onReceiveTestModeSnReadResponse
    		|| NULL == method_onReceiveTestModeFlagReadResponse || NULL == method_onReceiveTestModeSensorReadResponse
    		|| NULL == method_onReceiveTestButton
    		|| NULL == method_onReceiveOTAEnterOTAModeResponse || NULL == method_resetBleConnect
    		|| NULL == method_onReceiveRemoteControlCameraTakePicture || NULL == method_onReceiveRemoteControlSingleClick
    		|| NULL == method_onReceiveRemoteControlDoubleClick) {
    	ALOGE(" classInitNative get method NULL\n");
    }
}

static jint initNative(JNIEnv* env, jobject obj) {
    int ret;

    ALOGV(" initNative\n");

    sBlueToothObj = env->NewGlobalRef(obj);

    env->GetJavaVM(&vm);

    return 0;
}

pthread_mutex_t bleStackMutex;
pthread_mutexattr_t attr;

static void lock_stack_l2() {
	ALOGV(" lock_stack_l2\n");
	pthread_mutex_lock(&bleStackMutex );
}

static void unlock_stack_l2() {
	ALOGV(" unlock_stack_l2\n");
	pthread_mutex_unlock(&bleStackMutex );
}

static jint initBleStackNative(JNIEnv* env, jobject obj) {
    int ret;

    ALOGV(" initBleStackNative\n");

	pthread_mutexattr_init(&attr);

	pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_RECURSIVE_NP);

	pthread_mutex_init(&bleStackMutex, &attr);

	pthread_mutexattr_destroy(&attr);

    ret = init_health_ble_L2(L2_recv_callback,L2_send_callback,L2_create_timer,L2_stop_timer,lock_stack_l2,unlock_stack_l2,need_reset_l2);

    return ret;
}

static jint finalizeBleStackNative(JNIEnv* env, jobject obj) {
    int ret;

    ALOGV(" finalizeBleStackNative\n");

    ret = finalize_health_ble_L2();

    pthread_mutex_destroy(&bleStackMutex);

    return ret;
}

static jint testEchoRequestNative(JNIEnv* env, jobject obj,jbyteArray data) {
	int ret;

	unsigned short length = env->GetArrayLength(data);

	ALOGV("testEchoRequestNative ");

	L2_command_test_echo_request_t *test_echo_request =
			(L2_command_test_echo_request_t*) malloc(
					sizeof(L2_command_test_echo_request_t));
	if (NULL == test_echo_request) {
		ALOGE("malloc test_echo_request error ");
		return -1;
	}

	memset(test_echo_request,0,sizeof(L2_command_test_echo_request_t));
	test_echo_request->common.key = ECHO_REQUEST;
	test_echo_request->length = length;

	test_echo_request->data = (char*)malloc(test_echo_request->length);

	if(NULL == test_echo_request->data) {
		ALOGE("malloc test_echo_request error ");
		free_key_common(HEALTH_BLE_COMMAND_FACTORY_TEST,(L2_key_common_t*)test_echo_request);
		return -1;
	}

	env->GetByteArrayRegion(data, 0, length, (jbyte*)(test_echo_request->data));

	return sendCommandWithKey(HEALTH_BLE_COMMAND_FACTORY_TEST,0,(L2_key_common*)test_echo_request);
}

static jint testChargeRequestNative(JNIEnv* env, jobject obj) {

	ALOGV("testChargeRequestNative ");
	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_FACTORY_TEST,0,CHARGE_REUQEST);

}

static jint testLedOnRequestNative(JNIEnv* env, jobject obj,jint have_mode,jint mode) {
	int ret;
	ALOGV("testLedOnRequestNative ");

	L2_command_test_led_request_t *test_led_request =
			(L2_command_test_led_request_t*) malloc(
					sizeof(L2_command_test_led_request_t));
	if (NULL == test_led_request) {
		ALOGE("malloc test_led_request error ");
		return -1;
	}

	test_led_request->common.key = LED_OPERATION;

	test_led_request->have_mode = have_mode;
	test_led_request->mode = mode;


	return sendCommandWithKey(HEALTH_BLE_COMMAND_FACTORY_TEST,0,(L2_key_common_t*)test_led_request);

}

static jint testVibrateRequestNative(JNIEnv* env, jobject obj) {
	ALOGV("testVibrateRequestNative ");
	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_FACTORY_TEST,0,VIBRATE_OPERATION);
}

static jint testWriteSnRequestNative(JNIEnv* env, jobject obj,jbyteArray sn) {
	int ret;

	unsigned short length = env->GetArrayLength(sn);

	ALOGV("testWriteSnRequestNative ");

	L2_command_test_sn_write_request_t *test_sn_write_request =
			(L2_command_test_sn_write_request_t*) malloc(
					sizeof(L2_command_test_sn_write_request_t));
	if (NULL == test_sn_write_request) {
		ALOGE("malloc test_sn_write_request error ");
		return -1;
	}
	memset(test_sn_write_request, 0,
			sizeof(L2_command_test_sn_write_request_t));

	test_sn_write_request->common.key = SN_WRITE;

	env->GetByteArrayRegion(sn, 0, length,
			(jbyte*) (test_sn_write_request->sn));

	return sendCommandWithKey(HEALTH_BLE_COMMAND_FACTORY_TEST, 0,
			(L2_key_common_t*)test_sn_write_request);
}

static jint testReadSnRequestNative(JNIEnv* env, jobject obj) {
	ALOGV("testReadSnRequestNative ");
	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_FACTORY_TEST,0,SN_READ);
}

static jint testWriteFlagRequest(JNIEnv* env, jobject obj,jbyte flag) {
	int ret;

	ALOGV("testWriteFlagRequest ");

	L2_command_test_flag_write_request_t *test_write_flag_request =
			(L2_command_test_flag_write_request_t*) malloc(
					sizeof(L2_command_test_flag_write_request_t));
	if (NULL == test_write_flag_request) {
		ALOGE("malloc test_write_flag_request error ");
		return -1;
	}
	memset(test_write_flag_request, 0,
			sizeof(L2_command_test_flag_write_request_t));

	test_write_flag_request->common.key = TEST_FLAG_WRITE;

	test_write_flag_request->test_flag = flag;

	return sendCommandWithKey(HEALTH_BLE_COMMAND_FACTORY_TEST, 0,
			(L2_key_common_t*)test_write_flag_request);
}

static jint testReadFlagRequestNative(JNIEnv* env, jobject obj) {
	ALOGV("testReadFlagRequestNative ");
	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_FACTORY_TEST,0,TEST_FLAG_READ);
}
static jint testReadSensorRequestNative(JNIEnv* env, jobject obj) {
	ALOGV("testReadSensorRequestNative ");
	return sendCommandWithEmptyValueKey(HEALTH_BLE_COMMAND_FACTORY_TEST,0,SENSOR_DATA_REQUEST);
}
static jint testEnterTestModeRequestNative(JNIEnv* env, jobject obj) {

	ALOGV("testEnterTestModeRequestNative ");

	L2_command_test_mode_enter_resquest_t *test_mode_enter_request =
			(L2_command_test_mode_enter_resquest_t*) malloc(
					sizeof(L2_command_test_mode_enter_resquest_t));
	if (NULL == test_mode_enter_request) {
		ALOGE("malloc test_mode_enter_request error ");
		return -1;
	}
	memset(test_mode_enter_request, 0,
			sizeof(L2_command_test_mode_enter_resquest_t));

	test_mode_enter_request->common.key = TEST_MODE_ENTER;

	memcpy(test_mode_enter_request->token, "1234567890",
			sizeof("1234567890"));

	return sendCommandWithKey(HEALTH_BLE_COMMAND_FACTORY_TEST, 0,
			(L2_key_common_t*)test_mode_enter_request);
}

static jint testExitTestModeRequestNative(JNIEnv* env, jobject obj) {
	ALOGV("testExitTestModeRequestNative ");

	L2_command_test_mode_exit_resquest_t *test_mode_exit_request =
			(L2_command_test_mode_exit_resquest_t*) malloc(
					sizeof(L2_command_test_mode_exit_resquest_t));
	if (NULL == test_mode_exit_request) {
		ALOGE("malloc test_mode_enter_request error ");
		return -1;
	}
	memset(test_mode_exit_request, 0,
			sizeof(L2_command_test_mode_exit_resquest_t));

	test_mode_exit_request->common.key = TEST_MODE_EXIT;

	memcpy(test_mode_exit_request->token, "1234567890",
			sizeof(test_mode_exit_request->token));

	return sendCommandWithKey(HEALTH_BLE_COMMAND_FACTORY_TEST, 0,
			(L2_key_common_t*)test_mode_exit_request);
}



static jint testSetMotorBurnInRequestNative(JNIEnv* env, jobject obj,jboolean enable) {
	int ret;

	ALOGV("testSetMotorBurnInRequest ");

	L2_command_test_motor_burn_in_t *test_motor_burn_in =
			(L2_command_test_motor_burn_in_t*) malloc(
					sizeof(L2_command_test_motor_burn_in_t));
	if (NULL == test_motor_burn_in) {
		ALOGE("malloc test_motor_burn_in error ");
		return -1;
	}
	memset(test_motor_burn_in, 0,
			sizeof(L2_command_test_motor_burn_in_t));

	test_motor_burn_in->common.key = MOTOR_BURN_IN_TEST;
	
	if(true == enable) {
		test_motor_burn_in->enable = MOTOR_BURN_IN_TEST_ENABLED;
	} else {
		test_motor_burn_in->enable = MOTOR_BURN_IN_TEST_DISABLED;
	}

	return sendCommandWithKey(HEALTH_BLE_COMMAND_FACTORY_TEST, 0,
			(L2_key_common_t*)test_motor_burn_in);
}

static jint testSetLedBurnInRequestNative(JNIEnv* env, jobject obj,jboolean enable) {
	int ret;

	ALOGV("testSetLedBurnInRequest ");

	L2_command_test_led_burn_in_t *test_led_burn_in =
			(L2_command_test_led_burn_in_t*) malloc(
					sizeof(L2_command_test_led_burn_in_t));
	if (NULL == test_led_burn_in) {
		ALOGE("malloc test_led_burn_in error ");
		return -1;
	}
	memset(test_led_burn_in, 0,
			sizeof(L2_command_test_led_burn_in_t));

	test_led_burn_in->common.key = LED_BURN_IN_TEST;
	
	if(true == enable) {
		test_led_burn_in->enable = LED_BURN_IN_TEST_ENABLED;
	} else {
		test_led_burn_in->enable = LED_BURN_IN_TEST_DISABLED;
	}

	return sendCommandWithKey(HEALTH_BLE_COMMAND_FACTORY_TEST, 0,
			(L2_key_common_t*)test_led_burn_in);
}


jint remoteControlCameraStateNative(JNIEnv* env, jobject obj, jint state) {
	int ret;

		ALOGV("remoteControlCameraStateNative ");

		L2_command_remote_control_camera_state_request_t *camera_state_request =
				(L2_command_remote_control_camera_state_request_t*) malloc(
						sizeof(L2_command_remote_control_camera_state_request_t));
		if (NULL == camera_state_request) {
			ALOGE("malloc v error ");
			return -1;
		}

		camera_state_request->common.key = CAMERA_APP_STATE_REQUEST;

		camera_state_request->state = state;


		return sendCommandWithKey(HEALTH_BLE_COMMAND_REMOTE_CONTROL,0,(L2_key_common_t*)camera_state_request);
}


static JNINativeMethod sMethods[] = {
    /* name, signature, funcPtr */
    {"classInitNative", "()V", (void *) classInitNative},
    {"initNative", "()I", (void *) initNative},
    {"initBleStackNative", "()I", (void *) initBleStackNative},
    {"finalizeBleStackNative", "()I", (void *) finalizeBleStackNative},
    {"otaEnterOTAModeNative", "()I", (void *) otaEnterOTAModeNative},
    {"bindNative", "([B)I", (void *) bindNative},
    {"loginNative", "([B)I", (void *) loginNative},
    {"setTimeNative", "(IIIIII)I", (void *) setTimeNative},
    {"setAlarmListNative", "(Lcom/baidu/wearable/ble/model/ClockList;)I", (void *) setAlarmListNative},
    {"getAlarmListNative", "()I", (void *) getAlarmListNative},
    {"setSportTargetNative", "(I)I", (void *) setSportTargetNative},
    {"setUserProfileNative", "(ZIII)I", (void *) setUserProfileNative},
    {"setLinklostNative", "(I)I", (void *) setLinklostNative},
    {"setStillAlarmNative", "(IIIIIZZZZZZZ)I", (void *) setStillAlarmNative},
    {"setLeftOrRightHandNative", "(Z)I", (void *) setLeftOrRightHandNative},
    {"setOperationSystemNative", "(II)I", (void *) setOperationSystemNative},
    {"phoneCommingNative", "()I", (void *) phoneCommingNative},
    {"phoneAnswerNative", "()I", (void *) phoneAnswerNative},
    {"phoneDenyNative", "()I", (void *) phoneDenyNative},
    {"requestDataNative", "()I", (void *) requestDataNative},
    {"setDataSyncNative", "(I)I", (void *) setDataSyncNative},
    
    {"setDailySportDataNative", "(III)I", (void *) setDailySportDataNative},

    {"testEchoRequestNative", "([B)I", (void *) testEchoRequestNative},
    {"testChargeRequestNative", "()I", (void *) testChargeRequestNative},
    {"testLedOnRequestNative", "(II)I", (void *) testLedOnRequestNative},

    {"testVibrateRequestNative", "()I", (void *) testVibrateRequestNative},
    {"testWriteSnRequestNative", "([B)I", (void *) testWriteSnRequestNative},
    {"testReadSnRequestNative", "()I", (void *) testReadSnRequestNative},
    {"testWriteFlagRequestNative", "(B)I", (void *) testWriteFlagRequest},

    {"testReadFlagRequestNative", "()I", (void *) testReadFlagRequestNative},
    {"testReadSensorRequestNative", "()I", (void *) testReadSensorRequestNative},
    {"testEnterTestModeRequestNative", "()I", (void *) testEnterTestModeRequestNative},
    {"testExitTestModeRequestNative", "()I", (void *) testExitTestModeRequestNative},
    
    {"testSetMotorBurnInRequestNative", "(Z)I", (void *) testSetMotorBurnInRequestNative},
    {"testSetLedBurnInRequestNative", "(Z)I", (void *) testSetLedBurnInRequestNative},
    

    {"remoteControlCameraStateNative", "(I)I", (void *) remoteControlCameraStateNative},

    {"bleStackTimeFireNative", "()V", (void *) bleStackTimeFireNative},
};

int register_com_baidu_wearable_bluetooth_BlueTooth(JNIEnv* env)
{
    jclass clazz;
    clazz = env->FindClass("com/baidu/wearable/ble/stack/BlueTooth");
    return env->RegisterNatives( clazz,
                                    sMethods, NELEM(sMethods));
}

/*
 * JNI Initialization
 */

extern jint JNI_OnLoad_LO(JavaVM *jvm, void *reserved);

jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    JNIEnv *e;
    int status;

    ALOGV("bluetooth loading JNI\n");

    // Check JNI version
    if (jvm->GetEnv((void **)&e, JNI_VERSION_1_6)) {
        ALOGE("JNI version mismatch error");
        return JNI_ERR;
    }

    if ((status = register_com_baidu_wearable_bluetooth_BlueTooth(e)) < 0) {
        ALOGE("jni  registration failure, status: %d", status);
        return JNI_ERR;
    }
    JNI_OnLoad_LO(jvm,reserved);
    return JNI_VERSION_1_6;
}
