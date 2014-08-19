//
//  health-ble-stack-L0.c
//  baiduhealth
//
//  Created by Chen Xiaobin on 9/10/13.
//  Copyright (c) 2013 Baidu. All rights reserved.
//

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "health-ble-stack-L0.h"
#include "bit-order.h"

#if ANDROID
#include <android/log.h>

#define TAG "wearable_bluetooth"


#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__)
#elif TARGET_OS_IPHONE || TARGET_IPHONE_SIMULATOR
#if DEBUG
#include "health-ble-stack_c_bridge.h"
static char buffer_string[1000];
#define LOGI(...) do {sprintf(buffer_string, __VA_ARGS__);print_c_log_info(buffer_string);} while(0)
#define LOGE(...) do {sprintf(buffer_string, __VA_ARGS__);print_c_log_warning(buffer_string);} while(0)
#else
  #define LOGI(...) do {} while(0)
#define LOGE(...) do {} while(0)
#endif
#endif



recv_cb_L0 recv_cb_L0_ptr = NULL;
send_cb_L0 send_cb_L0_ptr = NULL;
write_data_L0_to_ble ble_handle_send = NULL;


void recv_cb_platform_implement(health_ble_data_L0_t * const data, int status_code) {
  LOGI("recv_cb_platform_implement");
  
  if(NULL != recv_cb_L0_ptr) {
    recv_cb_L0_ptr(data,status_code);
  }
  
}
void send_cb_platform_implement(uint16_t seq_id, int status_code){
  LOGI("send_cb_platform_implement");
  
  if(NULL != send_cb_L0_ptr) {
    send_cb_L0_ptr(seq_id,status_code);
  }
}

//typedef void(*write_data_L0_to_ble)(char * const data, size_t length, uint16_t seq_id);

#if TARGET_OS_IPHONE || TARGET_IPHONE_SIMULATOR

int init_health_ble_L0(recv_cb_L0 recv_cb, send_cb_L0 send_cb)
{
  LOGI("init_health_ble_L0");
  if (NULL == recv_cb || NULL ==  send_cb) {
    LOGI("recv_cb is NULL or send_cb is NULL");
    return -1;
  }

  recv_cb_L0_ptr = recv_cb;
  send_cb_L0_ptr = send_cb;
  
  return 0;
}

int finalize_health_ble_L0()
{
  
  LOGI("finalize_health_ble_L0");
  
	
  recv_cb_L0_ptr = NULL;
  send_cb_L0_ptr = NULL;
  
  return 0;
}

int send_L0(char * const data, size_t length, uint16_t seq_id)
{
  LOGI("%s called succeed, data:0x%x, length:%zu, seq_id:%d", __PRETTY_FUNCTION__, (int)data, length, seq_id);
  
  if (NULL == ble_handle_send) {
    LOGE("ble_handle_send is NULL");
    return -1;
  }

  return ble_handle_send(data, length, seq_id);
  
}



#elif ANDROID
/*
 *  define android methods below
 */
int init_health_ble_L0(recv_cb_L0 recv_cb, send_cb_L0 send_cb)
{
  
  LOGI("init_health_ble_L0");
  
	if (NULL == recv_cb || NULL == send_cb){
    LOGE("recv_cb is NULL or send_cb is NULL");
    return -1;
  }
  recv_cb_L0_ptr = recv_cb;
  send_cb_L0_ptr = send_cb;
  
  return 0;
}

int finalize_health_ble_L0()
{
  
  LOGI("finalize_health_ble_L0");
  
	
  recv_cb_L0_ptr = NULL;
  send_cb_L0_ptr = NULL;
  
  return 0;
}

int send_L0(char *data, size_t length, uint16_t seq_id)
{
  LOGI("send_L0 l0");
  
  if (NULL == ble_handle_send) {
    LOGI("ble_handle_send is NULL");
    return -1;
  }
  
  return ble_handle_send(data, length, seq_id);
}
#endif


int getL1CallBacks(recv_cb_L0 * const recv_cb, send_cb_L0 * const send_cb, write_data_L0_to_ble  call_send_data)
{
  LOGI("getL1CallBacks");
  
  if (!recv_cb || !send_cb || !call_send_data) {
    LOGE("getL1CallBacks error");
    return -1;
  }
  *recv_cb = recv_cb_platform_implement;
  *send_cb = send_cb_platform_implement;
  
  ble_handle_send = call_send_data;
  return 0;
}




