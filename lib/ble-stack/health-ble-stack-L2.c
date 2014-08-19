//
//  health-ble-stack-L2.c
//
//
//  Created by zhangdongsheng on 10/14/13.
//  Copyright (c) 2013 Baidu. All rights reserved.
//

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <assert.h>
#include "bit-order.h"
#include "health-ble-stack-L1.h"
#include "health-ble-stack-L2.h"


#define L2_DEBUG 1

#if (ANDROID && L2_DEBUG)

#include <android/log.h>
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "health-ble-stack-L2", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "health-ble-stack-L2", __VA_ARGS__))

#else
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

recv_cb_L2 recv_callback_l2 = NULL;
send_cb_L2 send_callback_l2 = NULL;

create_timer_L2 create_timer_l2 = NULL;
stop_timer_L2 stop_timer_l2 = NULL;
on_timer_fire_L1 on_timer_fire_l1_p = NULL;

lock_stack_L2 lock_stack_l2 = NULL;
unlock_stack_L2 unlock_stack_l2 = NULL;

need_reset_L2 need_reset_l2 = NULL;


static unsigned char have_been_init = 0;


void lock_l1_implement() {
//  LOGI("lock_l1_implement");
  if(NULL == lock_stack_l2) {
    LOGE("lock_stack_l2 is NULL");
    assert(0);
    return;
  }
  
  lock_stack_l2();
  
}

void unlock_l1_implement() {
//  LOGI("unlock_l1_implement");
  if(NULL == unlock_stack_l2) {
    LOGE("unlock_stack_l2 is NULL");
    assert(0);
    return;
  }
  
  unlock_stack_l2();
}


L2_command_t * malloc_command(uint16_t length, unsigned char *data,unsigned char **point,uint16_t *data_remain) {
    
  L2_command_t *command = (L2_command_t*)malloc(sizeof(L2_command_t));
  
  if(NULL == command) {
      LOGE("malloc_command malloc command error");
      return NULL;
  }
  
  memset(command,0,sizeof(L2_command_t));
  
  INIT_LIST_HEAD(&(command->key_list));
  *point = data;
  *data_remain = length;

  command->header.command_id = data[0];
  command->header.command_version = data[1];
  
  (*point) += 2;
  (*data_remain) -= 2;

  return command;
    
}

void copy_ota_enter_ota_mode_response(L2_command_ota_enter_ota_mode_response_t *enter_ota_mode_response,L2_command_t *new_command) {
  L2_command_ota_enter_ota_mode_response_t *new_enter_ota_mode_response;
  
  LOGI("copy_ota_enter_ota_mode_response");
  
  if(NULL == enter_ota_mode_response || NULL == new_command) {
    return;
  }
  
  new_enter_ota_mode_response = malloc(sizeof(L2_command_ota_enter_ota_mode_response_t));
  if(NULL == new_enter_ota_mode_response) {
    assert(0);
    return;
  }
  
  memcpy(new_enter_ota_mode_response, enter_ota_mode_response, sizeof(L2_command_ota_enter_ota_mode_response_t));
  
  list_add_tail(&(new_enter_ota_mode_response->common.list), &(new_command->key_list));
  
  return;
}


void copy_sport_data_sleep(L2_command_sport_data_response_sleep_t *sport_data_sleep,L2_command_t *new_command) {
  L2_command_sport_data_response_sleep_t *new_sport_data_sleep;
  
  LOGI("copy_sport_data_sleep");
  
  if(NULL == sport_data_sleep || NULL == new_command) {
    return;
  }
  
  new_sport_data_sleep = (L2_command_sport_data_response_sleep_t*)malloc(sizeof(L2_command_sport_data_response_sleep_t));
  if(NULL == new_sport_data_sleep) {
    LOGE("malloc new_sport_data_sleep error");
    assert(0);
    return;
  }
  
  memcpy(new_sport_data_sleep, sport_data_sleep, sizeof(L2_command_sport_data_response_sleep_t));
  
  INIT_LIST_HEAD(&(new_sport_data_sleep->sleep_list));
  
  list_add_tail(&(new_sport_data_sleep->common.list), &(new_command->key_list));
  
  struct list_head *p;
  list_for_each(p, &(sport_data_sleep->sleep_list)){
    L2_sleep_item_t *sleep_item,*new_sleep_item;
    sleep_item = (L2_sleep_item_t*)p;
    new_sleep_item = (L2_sleep_item_t*)malloc(sizeof(L2_sleep_item_t));
    
    if(NULL == new_sleep_item) {
      LOGE("malloc new_sleep_item error");
      assert(0);
      return;
    }
    
    memcpy(new_sleep_item,sleep_item,sizeof(L2_sleep_item_t));
    
    list_add_tail(&(new_sleep_item->list), &(new_sport_data_sleep->sleep_list));
    
  }
  
  return;

}


void copy_sport_data_sport(L2_command_sport_data_response_sport_t *sport_data_sport,L2_command_t *new_command) {
  L2_command_sport_data_response_sport_t *new_sport_data_sport;
  
  LOGI("copy_sport_data_sport");
  
  if(NULL == sport_data_sport || NULL == new_command) {
    return;
  }
  
  new_sport_data_sport = (L2_command_sport_data_response_sport_t*)malloc(sizeof(L2_command_sport_data_response_sport_t));
  if(NULL == new_sport_data_sport) {
    LOGE("malloc new_sport_data_sport error");
    assert(0);
    return;
  }
  
  memcpy(new_sport_data_sport, sport_data_sport, sizeof(L2_command_sport_data_response_sport_t));
  
  INIT_LIST_HEAD(&(new_sport_data_sport->sport_list));
  
  list_add_tail(&(new_sport_data_sport->common.list), &(new_command->key_list));
  
  struct list_head *p;
  list_for_each(p, &(sport_data_sport->sport_list)){
    L2_sport_item_t *sport_item,*new_sport_item;
    sport_item = (L2_sport_item_t*)p;
    new_sport_item = (L2_sport_item_t*)malloc(sizeof(L2_sport_item_t));
    
    if(NULL == new_sport_item) {
      LOGE("malloc new_sport_item error");
      assert(0);
      return;
    }
    
    memcpy(new_sport_item,sport_item,sizeof(L2_sport_item_t));
    
    list_add_tail(&(new_sport_item->list), &(new_sport_data_sport->sport_list));
    
  }
  
  return;
  
}


void copy_sport_data_sleep_setting(L2_command_sport_data_response_sleep_setting_t *sport_data_sleep_setting,L2_command_t *new_command) {
  L2_command_sport_data_response_sleep_setting_t *new_sport_data_sleep_setting;
  
  LOGI("copy_sport_data_sleep_setting");
  
  if(NULL == sport_data_sleep_setting || NULL == new_command) {
    return;
  }
  
  new_sport_data_sleep_setting = (L2_command_sport_data_response_sleep_setting_t*)malloc(sizeof(L2_command_sport_data_response_sleep_setting_t));
  if(NULL == new_sport_data_sleep_setting) {
    LOGE("malloc new_sport_data_sleep_setting error");
    assert(0);
    return;
  }
  
  memcpy(new_sport_data_sleep_setting, sport_data_sleep_setting, sizeof(L2_command_sport_data_response_sleep_setting_t));
  
  INIT_LIST_HEAD(&(new_sport_data_sleep_setting->sleep_setting_list));
  
  list_add_tail(&(new_sport_data_sleep_setting->common.list), &(new_command->key_list));
  
  struct list_head *p;
  list_for_each(p, &(sport_data_sleep_setting->sleep_setting_list)){
    L2_sleep_setting_item_t *sleep_setting_item,*new_sleep_setting_item;
    sleep_setting_item = (L2_sleep_setting_item_t*)p;
    new_sleep_setting_item = (L2_sleep_setting_item_t*)malloc(sizeof(L2_sleep_setting_item_t));
    
    if(NULL == new_sleep_setting_item) {
      LOGE("malloc new_sleep_setting_item error");
      assert(0);
      return;
    }
    
    memcpy(new_sleep_setting_item,sleep_setting_item,sizeof(L2_sleep_setting_item_t));
    
    list_add_tail(&(new_sleep_setting_item->list), &(new_sport_data_sleep_setting->sleep_setting_list));
    
  }
  
  return;
  
}

void copy_setting_alarm(L2_command_setting_alarm_t *setting_alarm,L2_command_t *new_command) {
  L2_command_setting_alarm_t *new_setting_alarm;
  
  LOGI("copy_setting_alarm");
  
  if(NULL == setting_alarm || NULL == new_command) {
    return;
  }
  
  new_setting_alarm = (L2_command_setting_alarm_t*)malloc(sizeof(L2_command_setting_alarm_t));
  if(NULL == new_setting_alarm) {
    LOGE("malloc new_setting_alarm error");
    assert(0);
    return;
  }
  
  memcpy(new_setting_alarm, setting_alarm, sizeof(L2_command_setting_alarm_t));
  
  INIT_LIST_HEAD(&(new_setting_alarm->alarm_list));
  
  list_add_tail(&(new_setting_alarm->common.list), &(new_command->key_list));
  
  struct list_head *p;
  list_for_each(p, &(setting_alarm->alarm_list)){
    L2_command_setting_alarm_item_t *alarm_item,*new_alarm_item;
    alarm_item = (L2_command_setting_alarm_item_t*)p;
    new_alarm_item = (L2_command_setting_alarm_item_t*)malloc(sizeof(L2_command_setting_alarm_item_t));
    
    if(NULL == new_alarm_item) {
      LOGE("malloc new_alarm_item error");
      assert(0);
      return;
    }
    
    memcpy(new_alarm_item,alarm_item,sizeof(L2_command_setting_alarm_item_t));
    
    list_add_tail(&(new_alarm_item->list), &(new_setting_alarm->alarm_list));
    
  }
  
  return;
}


void copy_get_alarm_response(L2_command_setting_get_alarm_response_t *get_alarm_response,L2_command_t *new_command) {
  L2_command_setting_get_alarm_response_t *new_get_alarm_response;
  
  LOGI("copy_get_alarm_response");
  
  if(NULL == get_alarm_response || NULL == new_command) {
    return;
  }
  
  new_get_alarm_response = (L2_command_setting_get_alarm_response_t*)malloc(sizeof(L2_command_setting_get_alarm_response_t));
  if(NULL == new_get_alarm_response) {
    LOGE("malloc new_get_alarm_response error");
    assert(0);
    return;
  }
  
  memcpy(new_get_alarm_response, get_alarm_response, sizeof(L2_command_setting_get_alarm_response_t));
  
  INIT_LIST_HEAD(&(new_get_alarm_response->alarm_list));
  
  list_add_tail(&(new_get_alarm_response->common.list), &(new_command->key_list));
  
  struct list_head *p;
  list_for_each(p, &(get_alarm_response->alarm_list)){
    L2_command_setting_alarm_item_t *alarm_item,*new_alarm_item;
    alarm_item = (L2_command_setting_alarm_item_t*)p;
    new_alarm_item = (L2_command_setting_alarm_item_t*)malloc(sizeof(L2_command_setting_alarm_item_t));
    
    if(NULL == new_alarm_item) {
      LOGE("malloc new_alarm_item error");
      assert(0);
      return;
    }
    
    memcpy(new_alarm_item,alarm_item,sizeof(L2_command_setting_alarm_item_t));
    
    list_add_tail(&(new_alarm_item->list), &(new_get_alarm_response->alarm_list));
    
  }
  
  return;
}

void copy_bind_bind_response(L2_command_bind_bind_response_t *bind_response,L2_command_t *new_command) {
  L2_command_bind_bind_response_t *new_bind_response;
  
  LOGI("copy_bind_bind_response");
  
  if(NULL == bind_response || NULL == new_command) {
    return;
  }
  
  new_bind_response = malloc(sizeof(L2_command_bind_bind_response_t));
  if(NULL == new_bind_response) {
    assert(0);
    return;
  }
  
  memcpy(new_bind_response, bind_response, sizeof(L2_command_bind_bind_response_t));
  
  list_add_tail(&(new_bind_response->common.list), &(new_command->key_list));
  
  return;
}


void copy_bind_login_response(L2_command_bind_login_response_t *login_response,L2_command_t *new_command) {
  L2_command_bind_login_response_t *new_login_response;
  
  LOGI("copy_bind_bind_response");
  
  if(NULL == login_response || NULL == new_command) {
    return;
  }
  
  new_login_response = malloc(sizeof(L2_command_bind_login_response_t));
  if(NULL == new_login_response) {
    assert(0);
    return;
  }
  
  memcpy(new_login_response, login_response, sizeof(L2_command_bind_login_response_t));
  
  list_add_tail(&(new_login_response->common.list), &(new_command->key_list));
  
  return;
}



void copy_key_common(L2_key_common_t *key_common,L2_command_t *new_command) {
  L2_key_common_t *new_key_common;
  
  LOGI("copy_key_common");
  
  if(NULL == key_common || NULL == new_command) {
    return;
  }
  
  new_key_common = malloc(sizeof(L2_key_common_t));
  if(NULL == new_key_common) {    
    assert(0);
    return;
  }
  
  memcpy(new_key_common, key_common, sizeof(L2_key_common_t));
  
  list_add_tail(&(new_key_common->list), &(new_command->key_list));
  
  return;
}


L2_command_t * copy_command(L2_command_t *command) {
 
  struct list_head *p;
  
  LOGI("copy_command");
  
  if(NULL == command) {
    return NULL;
  }
  
  L2_command_t *new_command = (L2_command_t*)malloc(sizeof(L2_command_t));
  
  if(NULL == new_command) {
    LOGE("copy_command malloc new_command error");
    return NULL;
  }
  
  memset(new_command,0,sizeof(L2_command_t));
  
  INIT_LIST_HEAD(&(new_command->key_list));
 
  memcpy(&(new_command->header), &(command->header),sizeof(new_command->header));
  
  
  switch(command->header.command_id) {
    case HEALTH_BLE_COMMAND_ROM_UPATE:
      list_for_each(p,&(command->key_list)) {
        L2_command_ota_enter_ota_mode_response_t *enter_ota_mode_response;
        switch(((L2_key_common_t*)p)->key) {
          case OTA_ENTER_OTA_MODE_RESPONSE:
            enter_ota_mode_response = (L2_command_ota_enter_ota_mode_response_t*)p;
            copy_ota_enter_ota_mode_response(enter_ota_mode_response,new_command);
            break;
          default:
            assert(0);
            break;
        }
      }
      break;
    case HEALTH_BLE_COMMAND_SETTING:
      list_for_each(p,&(command->key_list)) {
        L2_command_setting_alarm_t *setting_alarm;
        L2_command_setting_get_alarm_response_t *get_alarm_response;
        switch(((L2_key_common_t*)p)->key) {
          case SETTING_ALARM:
            setting_alarm = (L2_command_setting_alarm_t*)p;
            copy_setting_alarm(setting_alarm,new_command);
            break;
          case SETTING_GET_ALARM_RESPONSE:
            get_alarm_response = (L2_command_setting_get_alarm_response_t*)p;
            
            copy_get_alarm_response(get_alarm_response,new_command);
            break;
          default:
            assert(0);
            break;
        }
      }
      break;
    case HEALTH_BLE_COMMAND_BIND:
      list_for_each(p,&(command->key_list)) {
        L2_command_bind_bind_response_t *bind_response;
        L2_command_bind_login_response_t *login_response;
        switch(((L2_key_common_t*)p)->key) {
          case BIND_RESPONSE:
            bind_response = (L2_command_bind_bind_response_t*)p;
            copy_bind_bind_response(bind_response,new_command);
            break;
          case LOGIN_RESPONSE:
            login_response = (L2_command_bind_login_response_t*)p;
            
            copy_bind_login_response(login_response,new_command);
            break;
          default:
            assert(0);
            break;

        }
      }
      break;
    case HEALTH_BLE_COMMAND_ALARM:
      list_for_each(p,&(command->key_list)) {
        switch(((L2_key_common_t*)p)->key) {
          case PHONE_COMMING:
            copy_key_common((L2_key_common_t*)p,new_command);
            break;
          case PHONE_ANSWER:
            copy_key_common((L2_key_common_t*)p,new_command);
            break;
          case PHONE_DENY:
            copy_key_common((L2_key_common_t*)p,new_command);
            break;
          default:
            assert(0);
            break;
            
        }
      }
      break;
    case HEALTH_BLE_COMMAND_SPORT_DATA:
      list_for_each(p,&(command->key_list)) {
        L2_command_sport_data_response_sleep_t *sport_data_sleep;
        L2_command_sport_data_response_sport_t *sport_data_sport;
        L2_command_sport_data_response_sleep_setting_t *sport_data_sleep_setting;
        
        switch(((L2_key_common_t*)p)->key) {
          case DATA_RESPONSE_SLEEP:
            sport_data_sleep = (L2_command_sport_data_response_sleep_t*)p;
            copy_sport_data_sleep(sport_data_sleep,new_command);
            break;
          case DATA_RESPONSE_SPORT:
            sport_data_sport = (L2_command_sport_data_response_sport_t*)p;
            copy_sport_data_sport(sport_data_sport,new_command);
            break;
          case DATA_RESPONSE_SLEEP_SETTING:
            sport_data_sleep_setting = (L2_command_sport_data_response_sleep_setting_t*)p;
            
            copy_sport_data_sleep_setting(sport_data_sleep_setting,new_command);
            break;
          case DATA_RESPONSE_MORE:
            copy_key_common((L2_key_common_t*)p,new_command);
            break;
          case DATA_SYNC_START:
            copy_key_common((L2_key_common_t*)p,new_command);
            break;
          case DATA_SYNC_END:
            copy_key_common((L2_key_common_t*)p,new_command);
            break;
          default:
            assert(0);
            break;
        }
      
      }
      break;
    case HEALTH_BLE_COMMAND_FACTORY_TEST:
      assert(0);
      break;
    case HEALTH_BLE_COMMAND_REMOTE_CONTROL:
      assert(0);
      break;
    default:
      break;
      
  }

  
  return new_command;
}



void free_sport_data_sleep(L2_command_sport_data_response_sleep_t *sport_data_sleep) {
    if(NULL == sport_data_sleep) {
        return;
    }
    struct list_head *p,*n;
    list_for_each_safe(p, n, &(sport_data_sleep->sleep_list)){
        L2_sleep_item_t *sleep_item;
        list_del(p);
        sleep_item = (L2_sleep_item_t*)p;
        free(sleep_item);
    }
    
    return;
}

void free_sport_data_sport(L2_command_sport_data_response_sport_t *sport_data_sport) {
    if(NULL == sport_data_sport) {
        return;
    }
    struct list_head *p,*n;
    list_for_each_safe(p, n, &(sport_data_sport->sport_list)){
        L2_sport_item_t *sport_item;
        list_del(p);
        sport_item = (L2_sport_item_t*)p;
        free(sport_item);
    }
    
    return;
    
}

void free_sport_data_sleep_setting(L2_command_sport_data_response_sleep_setting_t *sport_data_sleep_setting) {
  if(NULL == sport_data_sleep_setting) {
    return;
  }
  struct list_head *p,*n;
  list_for_each_safe(p, n, &(sport_data_sleep_setting->sleep_setting_list)){
    L2_sleep_setting_item_t *sleep_setting_item;
    list_del(p);
    sleep_setting_item = (L2_sleep_setting_item_t*)p;
    free(sleep_setting_item);
  }
  
  return;
}

void free_setting_alarm(L2_command_setting_alarm_t *setting_alarm) {
  if(NULL == setting_alarm) {
    return;
  }
  struct list_head *p,*n;
  list_for_each_safe(p, n, &(setting_alarm->alarm_list)){
    L2_command_setting_alarm_item_t *alarm_item;
    list_del(p);
    alarm_item = (L2_command_setting_alarm_item_t*)p;
    free(alarm_item);
  }
  
  return;
}

void free_get_alarm_response(L2_command_setting_get_alarm_response_t *get_alarm_response) {
  if(NULL == get_alarm_response) {
    return;
  }
  struct list_head *p,*n;
  list_for_each_safe(p, n, &(get_alarm_response->alarm_list)){
    L2_command_setting_alarm_item_t *alarm_item;
    list_del(p);
    alarm_item = (L2_command_setting_alarm_item_t*)p;
    free(alarm_item);
  }
  
  return;
}

void free_key_common(unsigned char command_id,L2_key_common_t *key_common) {
  
  switch(command_id) {
    case HEALTH_BLE_COMMAND_ROM_UPATE:
      free(key_common);
      break;
    case HEALTH_BLE_COMMAND_SETTING:
      {
        L2_command_setting_alarm_t *setting_alarm;
        L2_command_setting_get_alarm_response_t *get_alarm_response;
        switch(key_common->key) {
          case SETTING_ALARM:
            setting_alarm = (L2_command_setting_alarm_t*)key_common;
            free_setting_alarm(setting_alarm);
            break;
          case SETTING_GET_ALARM_RESPONSE:
            get_alarm_response = (L2_command_setting_get_alarm_response_t*)key_common;
            
            free_get_alarm_response(get_alarm_response);
            break;
          default:
            break;
        }
        
        free(key_common);
      }
      break;
    case HEALTH_BLE_COMMAND_BIND:
      free(key_common);
      break;
    case HEALTH_BLE_COMMAND_ALARM:
      free(key_common);
      break;
    case HEALTH_BLE_COMMAND_SPORT_DATA:
      {
        L2_command_sport_data_response_sleep_t *sport_data_sleep;
        L2_command_sport_data_response_sport_t *sport_data_sport;
        L2_command_sport_data_response_sleep_setting_t *sport_data_sleep_setting;
        
        switch(key_common->key) {
          case DATA_RESPONSE_SPORT:
            sport_data_sleep = (L2_command_sport_data_response_sleep_t*)key_common;
            free_sport_data_sleep(sport_data_sleep);
            break;
          case DATA_RESPONSE_SLEEP:
            sport_data_sport = (L2_command_sport_data_response_sport_t*)key_common;
            
            free_sport_data_sport(sport_data_sport);
            break;
          case DATA_RESPONSE_SLEEP_SETTING:
            sport_data_sleep_setting = (L2_command_sport_data_response_sleep_setting_t*)key_common;
            
            free_sport_data_sleep_setting(sport_data_sleep_setting);
            break;
          default:
            break;
        }
        
        free(key_common);
      }
      break;
    case HEALTH_BLE_COMMAND_FACTORY_TEST:
    {
        L2_command_test_echo_request_t *echo_request;
        L2_command_test_echo_response_t *echo_response;
      
        switch(key_common->key) {
          case ECHO_REQUEST:
            echo_request = (L2_command_test_echo_request_t*)key_common;
            if(NULL != echo_request->data) {
              free(echo_request->data);
            }
            break;
          case ECHO_RESPONSE:
            echo_response = (L2_command_test_echo_response_t*)key_common;
            if(NULL != echo_response->data) {
              free(echo_response->data);
            }
            break;
          default:
            break;
        }
        
        free(key_common);
      }
      
      break;
      
    case HEALTH_BLE_COMMAND_REMOTE_CONTROL:
      free(key_common);
      break;
    default:
      break;
      
  }
}

void free_command(L2_command_t *command) {
  if(NULL == command) {
      return;
  }
  
  struct list_head *p, *n;
  
  switch(command->header.command_id) {
    case HEALTH_BLE_COMMAND_ROM_UPATE:
      list_for_each_safe(p,n,&(command->key_list)) {
        list_del(p);
        free_key_common(HEALTH_BLE_COMMAND_ROM_UPATE,(L2_key_common_t*)p);
      }
      break;
      break;
    case HEALTH_BLE_COMMAND_SETTING:
      list_for_each_safe(p,n,&(command->key_list)) {
        list_del(p);
        free_key_common(HEALTH_BLE_COMMAND_SETTING,(L2_key_common_t*)p);
      }
      break;
    case HEALTH_BLE_COMMAND_BIND:
      list_for_each_safe(p,n,&(command->key_list)) {
        list_del(p);
        free_key_common(HEALTH_BLE_COMMAND_BIND,(L2_key_common_t*)p);
      }
      break;
    case HEALTH_BLE_COMMAND_ALARM:
      list_for_each_safe(p,n,&(command->key_list)) {
        list_del(p);
        free_key_common(HEALTH_BLE_COMMAND_ALARM,(L2_key_common_t*)p);
      }
      break;
    case HEALTH_BLE_COMMAND_SPORT_DATA:
      list_for_each_safe(p,n,&(command->key_list)) {
        list_del(p);
        free_key_common(HEALTH_BLE_COMMAND_SPORT_DATA,(L2_key_common_t*)p);
      }
      break;
    case HEALTH_BLE_COMMAND_FACTORY_TEST:
      list_for_each_safe(p,n,&(command->key_list)) {
        list_del(p);
        free_key_common(HEALTH_BLE_COMMAND_FACTORY_TEST,(L2_key_common_t*)p);
      }

      break;
    case HEALTH_BLE_COMMAND_REMOTE_CONTROL:
      list_for_each_safe(p,n,&(command->key_list)) {
        list_del(p);
        free_key_common(HEALTH_BLE_COMMAND_REMOTE_CONTROL,(L2_key_common_t*)p);
      }
      
      break;
    default:
      break;
      
  }
  free(command);

  return;
    
}


unsigned char receive_command_ota_enter_ota_response(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_ota_enter_ota_mode_response_t *enter_ota_mode_response = NULL;
  uint16_t v_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_ota_enter_ota_response");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  enter_ota_mode_response = (L2_command_ota_enter_ota_mode_response_t*)malloc(sizeof(L2_command_ota_enter_ota_mode_response_t));
  if(NULL == enter_ota_mode_response) {
    LOGE("receive_command_ota_enter_ota_response malloc enter_ota_mode_response error");
    return 1;
  }
  memset(enter_ota_mode_response, 0, sizeof(L2_command_ota_enter_ota_mode_response_t));
  
  enter_ota_mode_response->common.key = OTA_ENTER_OTA_MODE_RESPONSE;
  
  list_add_tail(&(enter_ota_mode_response->common.list), &(command->key_list));
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  
  assert(2 == v_length);
  
  
  
  enter_ota_mode_response->status_code = **point;
  (*point) ++;
  (*data_remain) --;
  
  enter_ota_mode_response->error_code = **point;
  (*point) ++;
  (*data_remain) --;
  
  LOGI("status_code:0x%x, error_code:0x%x ",enter_ota_mode_response->status_code,enter_ota_mode_response->error_code);
   
  return 0;
}


L2_command_t* receive_command_ota(uint16_t length,unsigned char *data) {
  uint16_t data_remain = 0;
  unsigned char error_happen = 0;
  unsigned char *point = NULL;
  unsigned char key = 0;
  
  LOGI("receive_command_ota");
  
  if(length < 2) {
    LOGE("receive_command_ota data length error:%d",length);
    return NULL;
  }
  
  L2_command_t *command = malloc_command(length,data, &point, &data_remain);
  if(NULL == command) {
    return NULL;
  }
  
  while(data_remain > 0) {
    key = *((unsigned char*)point);
    point ++;
    data_remain --;
    switch(key) {
      case OTA_ENTER_OTA_MODE_RESPONSE:
        error_happen = receive_command_ota_enter_ota_response(command,&data_remain,&point);
        break;
      default:
        
        LOGE("receive_command_ota key not support:%d",key);
        error_happen = 1;
        assert(0);
        break;
        
    }
    
    if(error_happen) {
      break;
    }
    
  }
  
  if(error_happen) {
    free_command(command);
    command = NULL;
  }
  
  return command;
  
}


unsigned char receive_command_setting_get_alarm_response(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_setting_get_alarm_response_t *get_alarm_response = NULL;
  uint16_t v_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_setting_get_alarm_response");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  get_alarm_response = (L2_command_setting_get_alarm_response_t*)malloc(sizeof(L2_command_setting_get_alarm_response_t));
  if(NULL == get_alarm_response) {
    LOGE("receive_command_setting_get_alarm_response malloc get_alarm_response error");
    return 1;
  }
  memset(get_alarm_response, 0, sizeof(L2_command_setting_get_alarm_response_t));
  
  get_alarm_response->common.key = SETTING_GET_ALARM_RESPONSE;
  INIT_LIST_HEAD(&(get_alarm_response->alarm_list));
  
  list_add_tail(&(get_alarm_response->common.list), &(command->key_list));
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  
  while (*data_remain > 0 && v_length > 0) {
    uint32_t bit32_length;
    uint8_t bit8_length;
    L2_command_setting_alarm_item_t *alarm_item = (L2_command_setting_alarm_item_t*)malloc(sizeof(L2_command_setting_alarm_item_t));;
    
    if(NULL == alarm_item) {
      return 1;
    }
    
    memset(alarm_item,0,sizeof(L2_command_setting_alarm_item_t));
  
    bit32_length = (**point);
    (*point) ++;
    (*data_remain) --;
    v_length --;
    bit32_length = (bit32_length << 8) + (**point);
    (*point) ++;
    (*data_remain) --;
    v_length --;
    bit32_length = (bit32_length << 8) + (**point);
    (*point) ++;
    (*data_remain) --;
    v_length --;
    bit32_length = (bit32_length << 8) + (**point);
    (*point) ++;
    (*data_remain) --;
    v_length --;
    
    LOGI("receive_command_setting_get_alarm_response bit32_length:0x%x", bit32_length);
    alarm_item->year = (bit32_length&0xfC000000) >> 26;
    alarm_item->month = (bit32_length&0x03C00000) >> 22;
    alarm_item->day =  (bit32_length&0x003E0000) >> 17;
    alarm_item->hour = (bit32_length&0x0001F000) >> 12;
    alarm_item->minute = (bit32_length&0x00000FC0) >> 6;
    alarm_item->alarm_id = (bit32_length&0x00000038) >> 3;
    
    bit8_length = (**point);
    (*point) ++;
    (*data_remain) --;
    v_length --;
    
    alarm_item->Mon = (bit8_length >> 0)&0x1;
    alarm_item->Tue = (bit8_length >> 1)&0x1;
    alarm_item->Wed = (bit8_length >> 2)&0x1;
    alarm_item->Thu = (bit8_length >> 3)&0x1;
    alarm_item->Fri = (bit8_length >> 4)&0x1;
    alarm_item->Sat = (bit8_length >> 5)&0x1;
    alarm_item->Sun = (bit8_length >> 6)&0x1;
    
    LOGI("year:%d month:%d day:%d hour:%d minute:%d mon:%d tue:%d wed:%d thu:%d fri:%d sat:%d sun:%d alarm_id:%d",alarm_item->year,alarm_item->month,alarm_item->day,alarm_item->hour,alarm_item->minute,alarm_item->Mon,alarm_item->Tue,alarm_item->Wed,alarm_item->Thu,alarm_item->Fri,alarm_item->Sat,alarm_item->Sun,alarm_item->alarm_id);

    list_add_tail(&(alarm_item->list), &(get_alarm_response->alarm_list));
    
  }
  
  return 0;
}


L2_command_t* receive_command_setting(uint16_t length,unsigned char *data) {
  uint16_t data_remain = 0;
  unsigned char error_happen = 0;
  unsigned char *point = NULL;
  unsigned char key = 0;
  
  LOGI("receive_command_setting");
  
  if(length < 2) {
    LOGE("receive_command_setting data length error:%d",length);
    return NULL;
  }
  
  L2_command_t *command = malloc_command(length,data, &point, &data_remain);
  if(NULL == command) {
    return NULL;
  }
  
  while(data_remain > 0) {
    key = *((unsigned char*)point);
    point ++;
    data_remain --;
    switch(key) {
      case SETTING_GET_ALARM_RESPONSE:
        error_happen = receive_command_setting_get_alarm_response(command,&data_remain,&point);
        break;
      default:
        
        LOGE("receive_command_setting key not support:%d",key);
        error_happen = 1;
        assert(0);
        break;
        
    }
    
    if(error_happen) {
      break;
    }
    
  }
  
  if(error_happen) {
    free_command(command);
    command = NULL;
  }
  
  return command;
  
}



unsigned char receive_command_bind_bind_response(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_bind_bind_response_t *bind_bind_response = NULL;
  uint16_t v_length;
  unsigned char status_code;
  
  assert(*data_remain >= 3);
  
  LOGI("receive_command_bind_bind_response");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
      return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  LOGI("v_length:0x%x",v_length);
  assert(1 == v_length);
  
  status_code = **point;
  (*point) ++;
  (*data_remain) --;


  
  bind_bind_response = (L2_command_bind_bind_response_t*)malloc(sizeof(L2_command_bind_bind_response_t));
  if(NULL == bind_bind_response) {
      LOGE("receive_command_bind_bind_response malloc bind_bind_response error");
      return 1;
  }
  memset(bind_bind_response, 0, sizeof(L2_command_bind_bind_response_t));
  
  bind_bind_response->common.key = BIND_RESPONSE;
  
  bind_bind_response->status_code = status_code;
  
  list_add_tail(&(bind_bind_response->common.list), &(command->key_list));
  
  
  return 0;
}

unsigned char receive_command_bind_login_response(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_bind_login_response_t *bind_login_response = NULL;
  uint16_t v_length;
  unsigned char status_code;
  assert(*data_remain >= 3);
  
  LOGI("receive_command_bind_login_response");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  assert(1 == v_length);
  
  status_code = **point;
  (*point) ++;
  (*data_remain) --;
  
  bind_login_response = (L2_command_bind_login_response_t*)malloc(sizeof(L2_command_bind_login_response_t));
  if(NULL == bind_login_response) {
    LOGE("receive_command_bind_login_response malloc bind_login_response error");
    return 1;
  }
  memset(bind_login_response, 0, sizeof(L2_command_bind_login_response_t));
  
  bind_login_response->common.key = LOGIN_RESPONSE;
  
  bind_login_response->status_code = status_code;
  
  list_add_tail(&(bind_login_response->common.list), &(command->key_list));
  
  
  return 0;
}


L2_command_t* receive_command_bind(uint16_t length,unsigned char *data) {
    uint16_t data_remain = 0;
    unsigned char error_happen = 0;
    unsigned char *point = NULL;
    unsigned char key = 0;
    
    LOGI("receive_command_bind");

    if(length < 2) {
        LOGE("receive_command_bind data length error:%d",length);
        return NULL;
    }
    
    L2_command_t *command = malloc_command(length,data, &point, &data_remain);
    if(NULL == command) {
        return NULL;
    }
    
    while(data_remain > 0) {
        key = *((unsigned char*)point);
        point ++;
        data_remain --;
        switch(key) {
            case BIND_RESPONSE:
                error_happen = receive_command_bind_bind_response(command,&data_remain,&point);
                break;
            case LOGIN_RESPONSE:
                error_happen = receive_command_bind_login_response(command,&data_remain,&point);
                break;
            default:
                
                LOGE("receive_command_bind key not support:%d",key);
                error_happen = 1;
                assert(0);
                break;
                
        }
        
        if(error_happen) {
            break;
        }
        
    }
    
    if(error_happen) {
        free_command(command);
        command = NULL;
    }
    
    return command;

}

unsigned char receive_command_sport_data_response_sleep(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
    L2_command_sport_data_response_sleep_t *sport_data_sleep = NULL;
    uint16_t v_length;
    assert(*data_remain >= 2);
  
    LOGI("receive_command_sport_data_response_sleep");
    
    if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
        return 1;
    }
    
    sport_data_sleep = (L2_command_sport_data_response_sleep_t*)malloc(sizeof(L2_command_sport_data_response_sleep_t));
    if(NULL == sport_data_sleep) {
        LOGE("receive_command_bind_response_success malloc sport_data_sleep error");
        return 1;
    }
  
    memset(sport_data_sleep, 0, sizeof(L2_command_sport_data_response_sleep_t));
    
    sport_data_sleep->common.key = DATA_RESPONSE_SLEEP;
    INIT_LIST_HEAD(&(sport_data_sleep->sleep_list));
  
    list_add_tail(&(sport_data_sleep->common.list), &(command->key_list));
    
    v_length = **point;
    (*point) ++;
    (*data_remain) --;
    v_length = (v_length<<8) + (**point);
    (*point) ++;
    (*data_remain) --;
  
    while (*data_remain > 0 && v_length > 0) {
      uint16_t bit16_length;
      uint16_t i = 0;
      uint16_t count;
      uint16_t date;
//      char reserve;
      
      date = (**point);
      (*point) ++;
      (*data_remain) --;
      v_length --;
      date = (date << 8) + (**point);
      (*point) ++;
      (*data_remain) --;
      v_length --;
      
    
      sport_data_sleep->year = (date >> 9) &0x3f;
      sport_data_sleep->month = (date >> 5)&0x0f;
      sport_data_sleep->day = (date) &0x1f;
      
      LOGI("year:%d month:%d day:%d",sport_data_sleep->year,sport_data_sleep->month,sport_data_sleep->day);
      
      bit16_length = (**point);
      (*point) ++;
      (*data_remain) --;
      v_length --;
      bit16_length = (bit16_length << 8) + (**point);
      (*point) ++;
      (*data_remain) --;
      v_length --;
      
      count = bit16_length;
      
      LOGI("count:%d",count);
      
      assert((*data_remain) >= (4*count));
      assert(v_length == (4*count));
      
      for(i=0; i<count && (*data_remain) > 0 && v_length > 0; i++) {
        L2_sleep_item_t *sleep_item = (L2_sleep_item_t*)malloc(sizeof(L2_sleep_item_t));
        if(NULL == sleep_item){
          assert(0);
          return 1;
        }
        
        memset(sleep_item, 0, sizeof(L2_sleep_item_t));
       
        LOGI("i:%d",i);
        
        bit16_length = (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;
        bit16_length = (bit16_length << 8) + (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;
        
        sleep_item->minute  = bit16_length;
        
        bit16_length = (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;
        bit16_length = (bit16_length << 8) + (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;
        
        sleep_item->mode = bit16_length&0xF;
        
        
        LOGI("mode:%d,minute:%d",sleep_item->mode,sleep_item->minute);
        
        
        list_add_tail(&(sleep_item->list), &(sport_data_sleep->sleep_list));
      }
        
    }
    
    
    return 0;
}

unsigned char receive_command_sport_data_response_sport(L2_command_t *command,uint16_t *data_remain,unsigned char **point) {
    L2_command_sport_data_response_sport_t *sport_data_sport = NULL;
    uint16_t v_length;
    assert(*data_remain >= 2);
  
    LOGI("receive_command_sport_data_response_sport");
    
    if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
        return 1;
    }
    
    sport_data_sport = (L2_command_sport_data_response_sport_t*)malloc(sizeof(L2_command_sport_data_response_sport_t));
    if(NULL == sport_data_sport) {
        LOGE("receive_command_sport_data_response_sport malloc sport_data_sport error");
        return 1;
    }
  
    memset(sport_data_sport, 0, sizeof(L2_command_sport_data_response_sport_t));
    
    sport_data_sport->common.key = DATA_RESPONSE_SPORT;
    INIT_LIST_HEAD(&(sport_data_sport->sport_list));
  
    list_add_tail(&(sport_data_sport->common.list), &(command->key_list));
    
    v_length = **point;
    (*point) ++;
    (*data_remain) --;
    v_length = (v_length<<8) + (**point);
    (*point) ++;
    (*data_remain) --;
  
    LOGI("v_length:0x%x",v_length);
  
    while (*data_remain > 0 && v_length > 0) {
      uint16_t bit16_length;
      uint32_t bit32_length;
      uint16_t i = 0;
      unsigned char count;
      uint16_t date = (**point);
      char reserve;
      
      (*point) ++;
      (*data_remain) --;
      v_length --;
      date = (date << 8) + (**point);
      (*point) ++;
      (*data_remain) --;
      v_length --;
      

      sport_data_sport->year = (date >> 9) &0x3f;
      sport_data_sport->month = (date >> 5)&0x0f;
      sport_data_sport->day = (date) &0x1f;
      
      LOGI("year:%d month:%d day:%d",sport_data_sport->year,sport_data_sport->month,sport_data_sport->day);
    
      
      reserve = (**point);
      (*point) ++;
      (*data_remain) --;
      v_length --;
      
      count = **point;
      (*point) ++;
      (*data_remain) --;
      v_length --;
      
      LOGI("count:%d,data_remain:%d,v_length:0x%x",count,*data_remain,v_length);
      
      assert((*data_remain) >= (8*count));
      assert(v_length == (8*count));
      
      for(i=0; i<count && (*data_remain) > 0 && v_length > 0; i++) {
        L2_sport_item_t *sport_item = (L2_sport_item_t*)malloc(sizeof(L2_sport_item_t));
        
        if(NULL == sport_item) {
          assert(0);
          return 1;
        }
        
        memset(sport_item, 0, sizeof(L2_sport_item_t));
        
        LOGI("i:%d",i);
      
        bit32_length = **point;
        (*point) ++;
        (*data_remain) --;
        v_length --;
        bit32_length = (bit32_length<<8) + (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;
        sport_item->offset =  bit32_length>>5;
        
        sport_item->mode =  (bit32_length>>3)&0x3;
        
        bit32_length = bit32_length&0x7;
        
        bit32_length = (bit32_length<<8) + (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;
        bit32_length = (bit32_length << 8) + (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;
        
        sport_item->steps = (bit32_length >> 7)&0xfff;
        sport_item->active_time = (bit32_length >> 3)&0xf;
        
        bit32_length = bit32_length&0x7;
        
        bit32_length = (bit32_length<<8) + (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;
        bit32_length = (bit32_length << 8) + (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;
        
        
        sport_item->calory = bit32_length;
        
        bit16_length = (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;
        bit16_length = (bit16_length << 8) + (**point);
        (*point) ++;
        (*data_remain) --;
        v_length --;

        
        sport_item->distance = bit16_length;
        
        LOGI("offset:%d,mode:%d,steps:%d,calory:%d,distance:%d,active_time:%d",sport_item->offset,sport_item->mode,sport_item->steps,sport_item->calory,sport_item->distance,sport_item->active_time);
        
        list_add_tail(&(sport_item->list), &(sport_data_sport->sport_list));
      }
    }
  
    return 0;
}

unsigned char receive_command_sport_data_response_more(L2_command_t *command,uint16_t *data_remain,unsigned char **point) {
  L2_command_sport_data_response_more_t *sport_data_more = NULL;
  uint16_t v_length;
  assert(*data_remain >= 2);

  LOGI("receive_command_sport_data_response_more");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
      return 1;
  }
 
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;

  assert(0 == v_length);

  
  sport_data_more = (L2_command_sport_data_response_more_t*)malloc(sizeof(L2_command_sport_data_response_more_t));
  if(NULL == sport_data_more) {
      LOGE("receive_command_sport_data_response_more malloc sport_data_more error");
      return 1;
  }
  
  memset(sport_data_more, 0, sizeof(L2_command_sport_data_response_more_t));
  
  sport_data_more->common.key = DATA_RESPONSE_MORE;
  
  list_add_tail(&(sport_data_more->common.list), &(command->key_list));
  
  return 0;
}


unsigned char receive_command_sport_data_response_sleep_setting(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_sport_data_response_sleep_setting_t *sport_data_sleep_setting = NULL;
  uint16_t v_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_sport_data_response_sleep_setting");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  sport_data_sleep_setting = (L2_command_sport_data_response_sleep_setting_t*)malloc(sizeof(L2_command_sport_data_response_sleep_setting_t));
  if(NULL == sport_data_sleep_setting) {
    LOGE("receive_command_sport_data_response_sleep_setting malloc sport_data_sleep_setting error");
    return 1;
  }
  
  memset(sport_data_sleep_setting, 0, sizeof(L2_command_sport_data_response_sleep_setting_t));
  
  sport_data_sleep_setting->common.key = DATA_RESPONSE_SLEEP_SETTING;
  INIT_LIST_HEAD(&(sport_data_sleep_setting->sleep_setting_list));
  
  list_add_tail(&(sport_data_sleep_setting->common.list), &(command->key_list));
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  while (*data_remain > 0 && v_length > 0) {
    uint16_t bit16_length;
    uint16_t i = 0;
    uint16_t count;
    uint16_t date = (**point);
//    char reserve;
    
    (*point) ++;
    (*data_remain) --;
    v_length --;
    date = (date << 8) + (**point);
    (*point) ++;
    (*data_remain) --;
    v_length --;
    
  
    sport_data_sleep_setting->year = (date >> 9) &0x3f;
    sport_data_sleep_setting->month = (date >> 5)&0x0f;
    sport_data_sleep_setting->day = (date) &0x1f;
    
    LOGI("year:%d month:%d day:%d",sport_data_sleep_setting->year,sport_data_sleep_setting->month,sport_data_sleep_setting->day);
    
    
    bit16_length = (**point);
    (*point) ++;
    (*data_remain) --;
    v_length --;
    bit16_length = (bit16_length << 8) + (**point);
    (*point) ++;
    (*data_remain) --;
    v_length --;
    
    count = bit16_length;
    
    LOGI("count:%d",count);
    
    assert((*data_remain) >= (4*count));
    assert(v_length == (4*count));
    
    for(i=0; i<count && (*data_remain) > 0 && v_length > 0; i++) {
      L2_sleep_setting_item_t *sleep_setting_item = (L2_sleep_setting_item_t*)malloc(sizeof(L2_sleep_setting_item_t));
      
      if(NULL == sleep_setting_item) {
        assert(0);
        return 1;
      }
      
      memset(sleep_setting_item, 0, sizeof(L2_sleep_setting_item_t));
      
      LOGI("i:%d",i);
      
      bit16_length = (**point);
      (*point) ++;
      (*data_remain) --;
      v_length --;
      bit16_length = (bit16_length << 8) + (**point);
      (*point) ++;
      (*data_remain) --;
      v_length --;
      
      sleep_setting_item->minute  = bit16_length;
      
      bit16_length = (**point);
      (*point) ++;
      (*data_remain) --;
      v_length --;
      bit16_length = (bit16_length << 8) + (**point);
      (*point) ++;
      (*data_remain) --;
      v_length --;
      
      sleep_setting_item->mode = bit16_length&0xF;
      
      
      LOGI("mode:%d,minute:%d",sleep_setting_item->mode,sleep_setting_item->minute);
      
      
      list_add_tail(&(sleep_setting_item->list), &(sport_data_sleep_setting->sleep_setting_list));
    }
    
  }
  
  
  return 0;
}


unsigned char receive_command_sport_data_sync_start(L2_command_t *command,uint16_t *data_remain,unsigned char **point) {
  L2_command_sport_data_sync_start_t *sport_data_sync_start = NULL;
  uint16_t v_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_sport_data_sync_start");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  assert(0 == v_length);
  
  
  sport_data_sync_start = (L2_command_sport_data_sync_start_t*)malloc(sizeof(L2_command_sport_data_sync_start_t));
  if(NULL == sport_data_sync_start) {
    LOGE(" malloc sport_data_sync_start error");
    return 1;
  }
  
  memset(sport_data_sync_start, 0, sizeof(L2_command_sport_data_sync_start_t));
  
  sport_data_sync_start->common.key = DATA_SYNC_START;
  
  list_add_tail(&(sport_data_sync_start->common.list), &(command->key_list));
  
  return 0;
}


unsigned char receive_command_sport_data_sync_end(L2_command_t *command,uint16_t *data_remain,unsigned char **point) {
  L2_command_sport_data_sync_end_t *sport_data_sync_end = NULL;
  uint16_t v_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_sport_data_sync_end");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  assert(0 == v_length);
  
  
  sport_data_sync_end = (L2_command_sport_data_sync_end_t*)malloc(sizeof(L2_command_sport_data_sync_end_t));
  if(NULL == sport_data_sync_end) {
    LOGE(" malloc sport_data_sync_end error");
    return 1;
  }
  
  memset(sport_data_sync_end, 0, sizeof(L2_command_sport_data_sync_end_t));
  
  sport_data_sync_end->common.key = DATA_SYNC_END;
  
  list_add_tail(&(sport_data_sync_end->common.list), &(command->key_list));
  
  return 0;
}


L2_command_t* receive_command_sport_data(uint16_t length,unsigned char *data) {
  uint16_t data_remain = 0;
  unsigned char error_happen = 0;
  unsigned char *point = NULL;
  unsigned char key = 0;
  
  LOGI("receive_command_sport_data");
  
  if(length < 2) {
    LOGE("receive_command_sport_data data length error:%d",length);
    return NULL;
  }
  
  L2_command_t *command = malloc_command(length,data, &point, &data_remain);
  if(NULL == command) {
    return NULL;
  }
  
  while(data_remain) {
    key = *((unsigned char*)point);
    point ++;
    data_remain --;
    switch(key) {
      case DATA_RESPONSE_SLEEP:
          error_happen = receive_command_sport_data_response_sleep(command,&data_remain,&point);
          break;
      case DATA_RESPONSE_SPORT:
          error_happen = receive_command_sport_data_response_sport(command,&data_remain,&point);
          break;
      case DATA_RESPONSE_MORE:
          error_happen = receive_command_sport_data_response_more(command,&data_remain,&point);
          break;
      case DATA_RESPONSE_SLEEP_SETTING:
        error_happen = receive_command_sport_data_response_sleep_setting(command,&data_remain,&point);
        break;
      case DATA_SYNC_START:
        error_happen = receive_command_sport_data_sync_start(command,&data_remain,&point);
        break;
      case DATA_SYNC_END:
        error_happen = receive_command_sport_data_sync_end(command,&data_remain,&point);
        break;
      default:
          
          LOGE("receive_command_sport_data key not support:%d",key);
          error_happen = 1;
          assert(0);
          break;
            
    }
    
    if(error_happen) {
        break;
    }
      
  }
  
  if(error_happen) {
      free_command(command);
      command = NULL;
  }
  
  return command;
}

unsigned char receive_command_factory_test_echo_response(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_test_echo_response_t *echo_response = NULL;
  uint16_t v_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_factory_test_echo_response");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  
  echo_response = (L2_command_test_echo_response_t*)malloc(sizeof(L2_command_test_echo_response_t));
  if(NULL == echo_response) {
    LOGE("receive_command_factory_test_echo_response malloc echo_response error");
    return 1;
  }
  
  memset(echo_response,0,sizeof(L2_command_test_echo_response_t));
  
  echo_response->common.key = ECHO_RESPONSE;
  
  list_add_tail(&(echo_response->common.list), &(command->key_list));
  
  echo_response->length = v_length;
  echo_response->data = malloc(echo_response->length);
  
  if(NULL == echo_response->data) {
    assert(0);
    return 1;
  }
  
  memcpy(echo_response->data,(*point),echo_response->length);
  
  (*point) += echo_response->length;
  (*data_remain) -= echo_response->length;
  
  return 0;
}

unsigned char receive_command_factory_test_charge_response(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_test_charge_response_t *charge_response = NULL;
  uint16_t v_length;
  uint16_t bit16_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_factory_test_charge_response");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  assert(2 == v_length);
  
  charge_response = (L2_command_test_charge_response_t*)malloc(sizeof(L2_command_test_charge_response_t));
  if(NULL == charge_response) {
    LOGE("receive_command_factory_test_charge_response malloc charge_response error");
    return 1;
  }
  
  memset(charge_response,0,sizeof(L2_command_test_charge_response_t));
  
  charge_response->common.key = CHARGE_RESPONSE;
  
  
  bit16_length = **point;
  (*point) ++;
  (*data_remain) --;
  bit16_length = (bit16_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;

  charge_response->voltage = bit16_length;
  
  LOGI("voltage:%d",charge_response->voltage);
  
  list_add_tail(&(charge_response->common.list), &(command->key_list));
  
  return 0;
}

unsigned char receive_command_factory_test_sn_read_response(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_test_sn_read_response_t *sn_read_response = NULL;
  uint16_t v_length;
//  uint16_t bit16_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_factory_test_sn_read_response");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  assert(sizeof(sn_read_response->sn) == v_length);
  
  sn_read_response = (L2_command_test_sn_read_response_t*)malloc(sizeof(L2_command_test_sn_read_response_t));
  if(NULL == sn_read_response) {
    LOGE("receive_command_factory_test_sn_read_response malloc sn_read_response error");
    return 1;
  }
  
  memset(sn_read_response,0,sizeof(L2_command_test_sn_read_response_t));
  
  sn_read_response->common.key = SN_RESPONSE;
  
  memcpy(sn_read_response->sn,(*point),sizeof(sn_read_response->sn));
  
  (*point) += sizeof(sn_read_response->sn);
  (*data_remain) -= sizeof(sn_read_response->sn);
  
  list_add_tail(&(sn_read_response->common.list), &(command->key_list));
  
  return 0;
}

unsigned char receive_command_factory_test_flag_read_response(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_test_flag_read_response_t *flag_read_response = NULL;
  uint16_t v_length;
//  uint16_t bit16_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_factory_test_flag_read_response");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  
  assert(1 == v_length);
  
  flag_read_response = (L2_command_test_flag_read_response_t*)malloc(sizeof(L2_command_test_flag_read_response_t));
  if(NULL == flag_read_response) {
    LOGE("receive_command_factory_test_flag_read_response malloc flag_read_response error");
    return 1;
  }
  
  memset(flag_read_response,0,sizeof(L2_command_test_flag_read_response_t));
  
  flag_read_response->common.key = TEST_FLAG_RESPONSE;
  
  flag_read_response->test_flag = **point;
  (*point) ++;
  (*data_remain) --;
  
  LOGI("test_flag:%d",flag_read_response->test_flag);
  
  list_add_tail(&(flag_read_response->common.list), &(command->key_list));
  
  return 0;
}

unsigned char receive_command_factory_test_sensor_data_response(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_test_sensor_read_response_t *sensor_read_response = NULL;
  uint16_t v_length;
  uint16_t bit16_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_factory_test_sensor_data_response");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  assert(6 == v_length);
  
  sensor_read_response = (L2_command_test_sensor_read_response_t*)malloc(sizeof(L2_command_test_sensor_read_response_t));
  if(NULL == sensor_read_response) {
    LOGE("receive_command_factory_test_sensor_data_response malloc charge_response error");
    return 1;
  }
  
  memset(sensor_read_response,0,sizeof(L2_command_test_sensor_read_response_t));
  
  sensor_read_response->common.key = SENSOR_DATA_RESPONSE;
  
  bit16_length = **point;
  (*point) ++;
  (*data_remain) --;
  bit16_length = (bit16_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  sensor_read_response->x_axis = bit16_length;
  
  bit16_length = **point;
  (*point) ++;
  (*data_remain) --;
  bit16_length = (bit16_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  sensor_read_response->y_axis = bit16_length;
  
  bit16_length = **point;
  (*point) ++;
  (*data_remain) --;
  bit16_length = (bit16_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  sensor_read_response->z_axis = bit16_length;
  
  LOGI("x_axis:%d y_axis:%d z_axis:%d",sensor_read_response->x_axis,sensor_read_response->y_axis,sensor_read_response->z_axis);
  
  list_add_tail(&(sensor_read_response->common.list), &(command->key_list));
  
  return 0;
}

unsigned char receive_command_factory_test_button(L2_command_t *command,uint16_t *data_remain, unsigned char **point) {
  L2_command_test_button_t *test_button = NULL;
  uint16_t v_length;
  uint16_t bit16_length;
  uint32_t bit32_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_factory_test_button");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  assert(8 == v_length);
  
  test_button = (L2_command_test_button_t*)malloc(sizeof(L2_command_test_button_t));
  if(NULL == test_button) {
    LOGE("receive_command_factory_test_button malloc test_button error");
    return 1;
  }
  
  memset(test_button,0,sizeof(L2_command_test_button_t));
  
  test_button->common.key = BUTTON_TEST;
  
  test_button->code = **point;
  (*point) ++;
  (*data_remain) --;
  
  test_button->button_id = **point;
  (*point) ++;
  (*data_remain) --;
  
  //two byte reserved
  (*point) ++;
  (*data_remain) --;
  (*point) ++;
  (*data_remain) --;
  
  bit32_length = **point;
  (*point) ++;
  (*data_remain) --;
  bit32_length = (bit32_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  bit32_length = (bit32_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  bit32_length = (bit32_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  test_button->timestamp = bit32_length;
  
  
  
  LOGI("code:0x%x button_id:0x%x timestamp:0x%x",test_button->code,test_button->button_id,test_button->timestamp);
  
  list_add_tail(&(test_button->common.list), &(command->key_list));
  
  return 0;
}



L2_command_t* receive_command_factory_test(uint16_t length,unsigned char *data) {
  uint16_t data_remain = 0;
  unsigned char error_happen = 0;
  unsigned char *point = NULL;
  unsigned char key = 0;
  
  LOGI("receive_command_factory_test");
  
  if(length < 2) {
    LOGE("receive_command_factory_test data length error:%d",length);
    return NULL;
  }
  
  L2_command_t *command = malloc_command(length,data, &point, &data_remain);
  if(NULL == command) {
    return NULL;
  }
  
  while(data_remain) {
    key = *((unsigned char*)point);
    point ++;
    data_remain --;
    switch(key) {
      case ECHO_RESPONSE:
        error_happen = receive_command_factory_test_echo_response(command,&data_remain,&point);
        break;
      case CHARGE_RESPONSE:
        error_happen = receive_command_factory_test_charge_response(command,&data_remain,&point);
        break;
      case SN_RESPONSE:
        error_happen = receive_command_factory_test_sn_read_response(command,&data_remain,&point);
        break;
      case TEST_FLAG_RESPONSE:
        error_happen = receive_command_factory_test_flag_read_response(command,&data_remain,&point);
        break;
      case SENSOR_DATA_RESPONSE:
        error_happen = receive_command_factory_test_sensor_data_response(command,&data_remain,&point);
        break;
      case BUTTON_TEST:
        error_happen = receive_command_factory_test_button(command,&data_remain,&point);
        break;
      default:
        
        LOGE("receive_command_factory_test key not support:%d",key);
        error_happen = 1;
        assert(0);
        break;
        
    }
    
    if(error_happen) {
      break;
    }
    
  }
  
  if(error_happen) {
    free_command(command);
    command = NULL;
  }
  
  return command;
}

unsigned char receive_command_remote_control_camera_take_picture(L2_command_t *command,uint16_t *data_remain,unsigned char **point) {
  L2_command_remote_control_camera_take_picture_t *remote_control_camera_take_picture = NULL;
  uint16_t v_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_remote_control_camera_take_picture");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  assert(0 == v_length);
  
  
  remote_control_camera_take_picture = (L2_command_remote_control_camera_take_picture_t*)malloc(sizeof(L2_command_remote_control_camera_take_picture_t));
  if(NULL == remote_control_camera_take_picture) {
    LOGE("malloc remote_control_camera_take_picture error");
    return 1;
  }
  
  memset(remote_control_camera_take_picture, 0, sizeof(L2_command_remote_control_camera_take_picture_t));
  
  remote_control_camera_take_picture->common.key = CAMERA_TAKE_PICTURE;
  
  list_add_tail(&(remote_control_camera_take_picture->common.list), &(command->key_list));
  
  return 0;
}

unsigned char receive_command_remote_control_single_click(L2_command_t *command,uint16_t *data_remain,unsigned char **point) {
  L2_command_remote_control_single_click_t *remote_control_single_click = NULL;
  uint16_t v_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_remote_control_single_click");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  assert(0 == v_length);
  
  
  remote_control_single_click = (L2_command_remote_control_single_click_t*)malloc(sizeof(L2_command_remote_control_single_click_t));
  if(NULL == remote_control_single_click) {
    LOGE("malloc remote_control_single_click error");
    return 1;
  }
  
  memset(remote_control_single_click, 0, sizeof(L2_command_remote_control_single_click_t));
  
  remote_control_single_click->common.key = SINGLE_CLICK;
  
  list_add_tail(&(remote_control_single_click->common.list), &(command->key_list));
  
  return 0;
}

unsigned char receive_command_remote_control_double_click(L2_command_t *command,uint16_t *data_remain,unsigned char **point) {
  L2_command_remote_control_double_click_t *remote_control_double_click = NULL;
  uint16_t v_length;
  assert(*data_remain >= 2);
  
  LOGI("receive_command_remote_control_double_click");
  
  if(NULL == command || NULL == data_remain || NULL == point || NULL == *point) {
    return 1;
  }
  
  v_length = **point;
  (*point) ++;
  (*data_remain) --;
  v_length = (v_length<<8) + (**point);
  (*point) ++;
  (*data_remain) --;
  
  assert(0 == v_length);
  
  
  remote_control_double_click = (L2_command_remote_control_double_click_t*)malloc(sizeof(L2_command_remote_control_double_click_t));
  if(NULL == remote_control_double_click) {
    LOGE("malloc remote_control_single_click error");
    return 1;
  }
  
  memset(remote_control_double_click, 0, sizeof(L2_command_remote_control_double_click_t));
  
  remote_control_double_click->common.key = DOUBLE_CLICK;
  
  list_add_tail(&(remote_control_double_click->common.list), &(command->key_list));
  
  return 0;
}

L2_command_t* receive_command_remote_control(uint16_t length,unsigned char *data) {
  uint16_t data_remain = 0;
  unsigned char error_happen = 0;
  unsigned char *point = NULL;
  unsigned char key = 0;
  
  LOGI("receive_command_remote_control");
  
  if(length < 2) {
    LOGE("receive_command_remote_control data length error:%d",length);
    return NULL;
  }
  
  L2_command_t *command = malloc_command(length,data, &point, &data_remain);
  if(NULL == command) {
    return NULL;
  }
  
  while(data_remain) {
    key = *((unsigned char*)point);
    point ++;
    data_remain --;
    switch(key) {
      case CAMERA_TAKE_PICTURE:
        error_happen = receive_command_remote_control_camera_take_picture(command,&data_remain,&point);
        break;
      case SINGLE_CLICK:
        error_happen = receive_command_remote_control_single_click(command,&data_remain,&point);
        break;
      case DOUBLE_CLICK:
        error_happen = receive_command_remote_control_double_click(command,&data_remain,&point);
        break;
      default:
        
        LOGE("receive_command_remote_control key not support:%d",key);
        error_happen = 1;
        assert(0);
        break;
        
    }
    
    if(error_happen) {
      break;
    }
    
  }
  
  if(error_happen) {
    free_command(command);
    command = NULL;
  }
  
  return command;
}


void recv_cb_L1_implement(uint16_t length, unsigned char * data) {
  L2_command_t *command = NULL;
  unsigned char command_id;
  
  LOGI("recv_cb_L1_implement");
  
  if(0 == have_been_init) {
    LOGE("L2 have not been init");
    return;
  }

  if(NULL == data) {
      LOGE("recv_cb_L1_implement data is NULL");
      return;
  }
  
  command_id = data[0];
  
  switch(command_id) {
      case HEALTH_BLE_COMMAND_ROM_UPATE:
          command = receive_command_ota(length,data);
          break;
      case HEALTH_BLE_COMMAND_SETTING:
          command = receive_command_setting(length,data);
          break;
      case HEALTH_BLE_COMMAND_BIND:
          command = receive_command_bind(length,data);
          break;
      case HEALTH_BLE_COMMAND_SPORT_DATA:
          command = receive_command_sport_data(length,data);
          break;
      case HEALTH_BLE_COMMAND_FACTORY_TEST:
          command = receive_command_factory_test(length,data);
          break;
      case HEALTH_BLE_COMMAND_REMOTE_CONTROL:
          command = receive_command_remote_control(length,data);
          break;
      default:
          
          LOGE("recv_cb_L1_implement command command_id not support:%d",command_id);
          assert(0);
          break;
  }
  
  if(NULL != command) {
      LOGI("call recv_callback_l2");
      recv_callback_l2(command);
      
      free_command(command);
  }
  
  
  return;
}


void send_cb_L1_implement(uint16_t seq_id, int status_code) {
    
    LOGI("send_cb_L1_implement");
  
    if(0 == have_been_init) {
      LOGE("L2 have not been init");
      return;
    }
  
    if(NULL == send_callback_l2) {
        LOGE("send_cb_L1_implement have not been set");
        return;
    }
    
    LOGI("call send_callback_l2");
    send_callback_l2(seq_id,status_code);
    LOGI("call send_callback_l2 return");
  
    return;
}

unsigned char * malloc_L1_data() {
  unsigned char *data = NULL;
  data = malloc(L1_MTU);
  
  if(NULL == data) {
      LOGE("malloc_L1_data malloc data error");
      assert(0);
      return NULL;
  }
  
  return data;
}

void free_L1_data(unsigned char *data) {
  if(NULL == data) {
      return;
  }

  
  free(data);
  
  return;
}

unsigned char * handle_send_command_header(L2_command_t *command,uint16_t * length, unsigned char **point) {
  unsigned char *data;
  
  LOGI("handle_send_command_header command_id:0x%x command_version:0x%x",command->header.command_id,command->header.command_version);
  
  if(NULL == command) {
    LOGE("handle_send_command_header command is NULL");
    return NULL;
  }
  
  data = malloc_L1_data();
  
  if(NULL == data) {
    return NULL;
  }
  
  (*point) = data;
  *length = 0;
  
  memcpy(*point, &(command->header.command_id), sizeof(command->header.command_id));
  (*point) ++;
  (*length) ++;
  memcpy(*point, &(command->header.command_version), sizeof(command->header.command_version));
  (*point) ++;
  (*length) ++;
  
  return data;
}

void handle_send_no_value_key(L2_key_common_t *key_common,uint16_t * length, unsigned char **point){
  uint16_t bit16_length;
  
  if(NULL == key_common || NULL == length || NULL == point ) {
    return;
  }
  
  LOGI("handle_send_no_value_key key:0x%x ",key_common->key);
  
  (**point) = key_common->key;
  (*point) ++;
  (*length) ++;
  
  //v_length = 0;
  bit16_length = 0;
  bit16_length = cpu_2_be16(bit16_length);
  memcpy(*point, &bit16_length, 2);
  (*point) += 2;
  (*length) += 2;

}


unsigned char * send_ota_command(L2_command_t *command,uint16_t * length) {
  unsigned char *data;
  unsigned char *point;
  struct list_head *p;
  
  LOGI("send_ota_command");
  
  data = handle_send_command_header(command,length,&point);
  
  if(NULL == data) {
    length = 0;
    return NULL;
  }
  
  list_for_each(p, &(command->key_list)) {
    L2_key_common_t *key_common;
    
    key_common = (L2_key_common_t*)p;
    
    switch(key_common->key) {
      case OTA_ENTER_OTA_MODE:
        handle_send_no_value_key(key_common, length, &point);
        break;
      default:
        LOGE("send_ota_command key not support:%d",key_common->key);
        assert(0);
        break;
    }
    
  }
  
  return data;
}


unsigned char * send_setting_command(L2_command_t *command,uint16_t * length) {
  unsigned char *data;
  unsigned char *point;
  struct list_head *p,*sp;
  uint32_t bit32_length;
  uint16_t bit16_length;
  unsigned char alarm_count;
  unsigned char bit8_length;
  
  LOGI("send_setting_command");
  
  data = handle_send_command_header(command,length,&point);
  
  if(NULL == data) {
    length = 0;
    return NULL;
  }
  
  list_for_each(p, &(command->key_list)) {
    L2_key_common_t *key_common;
    L2_command_setting_time_t *setting_time;
    L2_command_setting_alarm_t *setting_alarm;
    L2_command_setting_sport_target_t* setting_sport_target;
    L2_command_setting_user_profile_t *setting_user_profile;
    L2_command_setting_link_lost_request_t *setting_link_lost_request;
    L2_command_setting_still_alarm_request_t *setting_still_alarm_request;
    L2_command_setting_left_or_right_hand_t *setting_left_or_right;
    L2_command_setting_os_t *setting_os;
    L2_command_setting_calling_contacts_white_list_t *setting_contacts_white_list;
    L2_command_setting_calling_notification_switch_t *setting_calling_notification_switch;;
    
    key_common = (L2_key_common_t*)p;
      
    switch(key_common->key) {
      case SETTING_TIME:
        
        LOGI("SETTING_TIME");
        
        setting_time = (L2_command_setting_time_t*)key_common;
    
        *point = SETTING_TIME;
        point ++;
        (*length) ++;
        
        bit32_length = (setting_time->year) << 26;
        bit32_length += (setting_time->month) << 22;
        bit32_length += (setting_time->day) << 17;
        bit32_length += (setting_time->hour) << 12;
        bit32_length += (setting_time->minute) << 6;
        bit32_length += (setting_time->second);
        
        LOGI("year:%d,month:%d,day:%d,hour:%d,minute:%d,second:%d",setting_time->year,setting_time->month,setting_time->day,setting_time->hour,setting_time->minute,setting_time->second);
        
        bit16_length = 4;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        
        bit32_length = cpu_2_be32(bit32_length);
        memcpy(point,&bit32_length, 4);
        point += 4;
        (*length) += 4;
        
        break;
      case SETTING_ALARM:
        LOGI("SETTING_ALARM");
        setting_alarm = (L2_command_setting_alarm_t*)key_common;
        
        *point = SETTING_ALARM;
        point ++;
        (*length) ++;
        
        alarm_count = 0;
        list_for_each(sp, &(setting_alarm->alarm_list)) {
          alarm_count++;
        }
        
        bit16_length = 5*alarm_count;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        list_for_each(sp, &(setting_alarm->alarm_list)) {
          L2_command_setting_alarm_item_t *alarm_item = (L2_command_setting_alarm_item_t*)sp;
          
          LOGI("year:%d month:%d day:%d hour:%d minute:%d mon:%d tue:%d wed:%d thu:%d fri:%d sat:%d sun:%d alarm_id:%d",alarm_item->year,alarm_item->month,alarm_item->day,alarm_item->hour,alarm_item->minute,alarm_item->Mon,alarm_item->Tue,alarm_item->Wed,alarm_item->Thu,alarm_item->Fri,alarm_item->Sat,alarm_item->Sun,alarm_item->alarm_id);
          
          bit32_length = (alarm_item->year&0x3f) << 26;
          bit32_length += (alarm_item->month&0xf) << 22;
          bit32_length += (alarm_item->day&0x1f) << 17;
          bit32_length += (alarm_item->hour&0x1f) << 12;
          bit32_length += (alarm_item->minute&0x3f) << 6;
          bit32_length += (alarm_item->alarm_id&0x7) << 3;
          
          bit32_length = cpu_2_be32(bit32_length);
          memcpy(point,&bit32_length, 4);
          point += 4;
          (*length) += 4;
          
          bit8_length = (alarm_item->Mon&0x1) << 0;
          bit8_length += (alarm_item->Tue&0x1)<< 1;
          bit8_length += (alarm_item->Wed&0x1)<< 2;
          bit8_length += (alarm_item->Thu&0x1)<< 3;
          bit8_length += (alarm_item->Fri&0x1)<< 4;
          bit8_length += (alarm_item->Sat&0x1)<< 5;
          bit8_length += (alarm_item->Sun&0x1)<< 6;
          
          //TODO may be a bit order error here
          *point = bit8_length;
          point ++;
          (*length) ++;
          
        }
        break;
        
      case SETTING_GET_ALARM_REQUEST:
        LOGI("SETTING_GET_ALARM_REQUEST");
        handle_send_no_value_key(key_common, length, &point);
        break;
        
      case SETTING_SPORT_TARGET:
        LOGI("SETTING_SPORT_TARGET");
        setting_sport_target = (L2_command_setting_sport_target_t*)key_common;
        
        *point = SETTING_SPORT_TARGET;
        point ++;
        (*length) ++;
        
        bit32_length = setting_sport_target->target;
        
        LOGI("target:%d",setting_sport_target->target);
        
        bit16_length = 4;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        bit32_length = cpu_2_be32(bit32_length);
        memcpy(point,&bit32_length, 4);
        point += 4;
        (*length) += 4;
        break;

        
      case SETTING_USER_PROFILE:
        LOGI("SETTING_USER_PROFILE");
        setting_user_profile = (L2_command_setting_user_profile_t*)key_common;
        
        *point = SETTING_USER_PROFILE;
        point ++;
        (*length) ++;
        
        bit32_length = (setting_user_profile->ismale&0x1) << 31;
        bit32_length += (setting_user_profile->age&0x7f) << 24;
        bit32_length += (setting_user_profile->height&0x1ff) << 15;
        bit32_length += (setting_user_profile->weight&0x3ff) << 5;
        
        LOGI("ismale:%d,age:%d,height:%d,weight:%d",setting_user_profile->ismale,setting_user_profile->age,setting_user_profile->height,setting_user_profile->weight);
        
        bit16_length = 4;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        bit32_length = cpu_2_be32(bit32_length);
        memcpy(point,&bit32_length, 4);
        point += 4;
        (*length) += 4;
        break;

      case SETTING_LINK_LOST_REQUEST:
        LOGI("SETTING_LINK_LOST_REQUEST");
        setting_link_lost_request = (L2_command_setting_link_lost_request_t*)key_common;
        
        *point = SETTING_LINK_LOST_REQUEST;
        point ++;
        (*length) ++;
        
        LOGI("alert_level:0x%x",setting_link_lost_request->alert_level);
        
        bit16_length = 1;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        *point = setting_link_lost_request->alert_level;
        point ++;
        (*length) ++;
       
        break;
        
      case SETTING_STILL_ALARM:
        LOGI("SETTING_STILL_ALARM");
        setting_still_alarm_request = (L2_command_setting_still_alarm_request_t*)key_common;
        
        *point = SETTING_STILL_ALARM;
        point ++;
        (*length) ++;
        
        LOGI("enabled:0x%x",setting_still_alarm_request->enable);
        LOGI("steps:0x%x",setting_still_alarm_request->steps);
        LOGI("minutes:0x%x",setting_still_alarm_request->minutes);
        LOGI("start_hour:0x%x",setting_still_alarm_request->start_hour);
        LOGI("end_hour:0x%x",setting_still_alarm_request->end_hour);
        LOGI("day_flag:0x%x",setting_still_alarm_request->day_flag);
        
        bit16_length = 8;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        //reserved
        point ++;
        (*length) ++;
        
        *point = setting_still_alarm_request->enable;
        point ++;
        (*length) ++;
        
        bit16_length = setting_still_alarm_request->steps;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        *point = setting_still_alarm_request->minutes;
        point ++;
        (*length) ++;
        
        *point = setting_still_alarm_request->start_hour;
        point ++;
        (*length) ++;
        
        *point = setting_still_alarm_request->end_hour;
        point ++;
        (*length) ++;
        
        *point = setting_still_alarm_request->day_flag;
        point ++;
        (*length) ++;
        
        break;
        
      case SETTING_LEFT_OR_RIGHT:
        setting_left_or_right = (L2_command_setting_left_or_right_hand_t*)p;
        LOGI("SETTING_LEFT_OR_RIGHT");
        
        if((*length)+ 3 + 1 > L1_MTU) {
          free_L1_data(data);
          return NULL;
        }
        
        *point = SETTING_LEFT_OR_RIGHT;
        point ++;
        (*length) ++;
        
        bit16_length = 1;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        *point = setting_left_or_right->value;
        point ++;
        (*length) ++;
        break;

      case SETTING_OS:
        setting_os = (L2_command_setting_os_t*)p;
        LOGI("SETTING_OS");

        *point = SETTING_OS;
        point++;
        (*length) ++;

        bit16_length = 2;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;

        *point = (unsigned char)setting_os->value;
        point++;
        (*length)++;

        *point = setting_os->reserved;
        point++;
        (*length)++;

        break;

      case SETTING_CONTACTS_WHITE_LIST:
        setting_contacts_white_list = (L2_command_setting_calling_contacts_white_list_t*)p;
        LOGI("SETTING_CONTACTS_WHITE_LIST");

        *point = SETTING_CONTACTS_WHITE_LIST;
        point++;
        (*length) ++;

        uint8_t contacts_count = 0;
        list_for_each(sp, &(setting_contacts_white_list->contacts)) {
          contacts_count++;
        }
        
        bit16_length = 1+20*contacts_count;   //1 byte for add or replace, 20 bytes for every contact
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;

        *point = (unsigned char)setting_contacts_white_list->action;
        point++;
        (*length) ++;

        list_for_each(sp, &(setting_contacts_white_list->contacts)) {
            L2_command_setting_calling_contacts_white_list_item_t *contact =
                    (L2_command_setting_calling_contacts_white_list_item_t*)sp;
            memcpy(point, contact->value, sizeof(contact->value));
            point += sizeof(contact->value);
            (*length) += sizeof(contact->value);
        }
        break;

      case SETTING_CALLING_NOTIFICATION_SWITCH:
        setting_calling_notification_switch = (L2_command_setting_calling_notification_switch_t*)p;
        LOGI("SETTING_CALLING_NOTIFICATION_SWITCH");

        *point = SETTING_CALLING_NOTIFICATION_SWITCH;
        point++;
        (*length) ++;

        bit16_length = 1;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;

        *point = setting_calling_notification_switch->value;
        point++;
        (*length)++;

        break;

      default:
          LOGE("send_setting_command key not support:%d",key_common->key);
          assert(0);
          break;
    }
      
  }
  
  return data;
}


unsigned char * send_bind_command(L2_command_t *command,uint16_t * length) {
  unsigned char *data;
  unsigned char *point;
  struct list_head *p;
  uint16_t bit16_length;
  
  LOGI("send_bind_command");
  
  data = handle_send_command_header(command,length,&point);
  
  if(NULL == data) {
    length = 0;
    return NULL;
  }
  
  list_for_each(p, &(command->key_list)) {
    L2_key_common_t *key_common;
    L2_command_bind_bind_request_t *bind_request;
    L2_command_bind_unbind_request_t *unbind_request;
    L2_command_bind_login_request_t *login_request;
    key_common = (L2_key_common_t*)p;
    
    switch(key_common->key) {
      case BIND_REQUEST:
        
        LOGI("BIND_REQUEST");
        bind_request = (L2_command_bind_bind_request_t*)key_common;
        *point = BIND_REQUEST;
        point ++;
        (*length) ++;
        
        //v_length = 32;
        bit16_length = 32;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;

        LOGI("userid:%s", bind_request->userid);
        memcpy(point,bind_request->userid, 32);
        point += 32;
        (*length) += 32;
        
        break;
      case LOGIN_REQUEST:
        LOGI("LOGIN_REQUEST");
        login_request = (L2_command_bind_login_request_t*)key_common;
        *point = LOGIN_REQUEST;
        point ++;
        (*length) ++;
        
        //v_length = 32;
        bit16_length = 32;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        LOGI("userid:%s", login_request->userid);
        memcpy(point,login_request->userid, 32);
        point += 32;
        (*length) += 32;
        
        break;
      case UNBIND_REQUEST:
        LOGI("UNBIND_REQUEST");
        unbind_request = (L2_command_bind_unbind_request_t*)key_common;
        *point = UNBIND_REQUEST;
        point ++;
        (*length) ++;

        //v_length = 1;
        bit16_length = 1;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;

        *point = unbind_request->reserved;
        point ++;
        (*length) ++;

        break;
      default:
          LOGE("send_bind_command key not support:%d",key_common->key);
          assert(0);
          break;
    }
      
  }
    
    return data;
}

unsigned char * send_alarm_command(L2_command_t *command,uint16_t * length) {
  unsigned char *data;
  unsigned char *point;
  struct list_head *p;
  
  LOGI("send_alarm_command");
  
  data = handle_send_command_header(command,length,&point);
  
  if(NULL == data) {
    length = 0;
    return NULL;
  }
  
  list_for_each(p, &(command->key_list)) {
    L2_key_common_t *key_common;
    
    key_common = (L2_key_common_t*)p;
    
    switch(key_common->key) {
      case PHONE_COMMING:
        handle_send_no_value_key(key_common, length, &point);
        break;
      case PHONE_ANSWER:
        handle_send_no_value_key(key_common, length, &point);
        break;
      case PHONE_DENY:
        handle_send_no_value_key(key_common, length, &point);
        break;
      default:
        LOGE("send_alarm_command key not support:%d",key_common->key);
        assert(0);
        break;
    }
    
  }
  
  return data;
}


unsigned char * send_sport_data_command(L2_command_t *command,uint16_t * length) {
  unsigned char *data;
  unsigned char *point;
  struct list_head *p;
  uint16_t bit16_length;
  uint32_t bit32_length;
  
  LOGI("send_sport_data_command");
  
  data = handle_send_command_header(command,length,&point);
  
  if(NULL == data) {
    length = 0;
    return NULL;
  }
    
  list_for_each(p, &(command->key_list)) {
    L2_key_common_t *key_common;
    L2_command_sport_data_sync_setting_t *sync_setting;
    L2_command_sport_daily_data_sync_t *daily_data_sync;
    
    key_common = (L2_key_common_t*)p;
    
    switch(key_common->key) {
      case DATA_REQUEST:
        handle_send_no_value_key(key_common, length, &point);
        break;
          
      case DATA_SYNC_SETTING:
        LOGI("DATA_SYNC_SETTING");
        
        sync_setting = (L2_command_sport_data_sync_setting_t*)key_common;
        *point = DATA_SYNC_SETTING;
        point ++;
        (*length) ++;
        
        //v_length = 1;
        bit16_length =1;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        LOGI("enbale %d",sync_setting->enable);
        
        *point = sync_setting->enable;
        point ++;
        (*length) ++;
        
          break;
        
      case DATA_DAILY_DATA_SYNC:
        LOGI("DATA_DAILY_DATA_SYNC");
        
        daily_data_sync = (L2_command_sport_daily_data_sync_t*)key_common;
        *point = DATA_DAILY_DATA_SYNC;
        point ++;
        (*length) ++;
        
        LOGI("daily_step:%d",daily_data_sync->daily_step);
        LOGI("daily_distance:%d",daily_data_sync->daily_distance);
        LOGI("daily_calory:%d",daily_data_sync->daily_calory);
       
        
        bit16_length = 12;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        
        bit32_length = cpu_2_be32(daily_data_sync->daily_step);
        memcpy(point,&bit32_length, 4);
        point += 4;
        (*length) += 4;
        
        bit32_length = cpu_2_be32(daily_data_sync->daily_distance);
        memcpy(point,&bit32_length, 4);
        point += 4;
        (*length) += 4;
        
        bit32_length = cpu_2_be32(daily_data_sync->daily_calory);
        memcpy(point,&bit32_length, 4);
        point += 4;
        (*length) += 4;
        
        break;
        
      default:
          LOGE("send_sport_data_command key not support:%d",key_common->key);
          assert(0);
          break;
    }
      
  }
  
  return data;
}


unsigned char * send_factory_test_command(L2_command_t *command,uint16_t * length) {
  unsigned char *data;
  unsigned char *point;
  struct list_head *p;
  uint16_t bit16_length;
  
  LOGI("send_factory_test_command");
  
  data = handle_send_command_header(command,length,&point);
  
  if(NULL == data) {
    length = 0;
    return NULL;
  }
  
  list_for_each(p, &(command->key_list)) {
    L2_key_common_t *key_common;
    L2_command_test_echo_request_t *echo_request;
    L2_command_test_led_request_t  *test_led;
    L2_command_test_sn_write_request_t *sn_write_request;
    L2_command_test_flag_write_request_t *test_flag_write_request;
    L2_command_test_mode_enter_resquest_t *test_mode_enter_request;
    L2_command_test_mode_exit_resquest_t *test_mode_exit_request;
    
    L2_command_test_motor_burn_in_t *test_motor_burn_in;
    L2_command_test_led_burn_in_t *test_led_burn_in;
    
    key_common = (L2_key_common_t*)p;
    
    switch(key_common->key) {
      case ECHO_REQUEST:
        echo_request = (L2_command_test_echo_request_t*)p;
        
        LOGI("ECHO_REQUEST");
        
        if((*length)+ 3 + echo_request->length > L1_MTU) {
          free_L1_data(data);
          return NULL;
        }
        
        *point = ECHO_REQUEST;
        point ++;
        (*length) ++;
        
        bit16_length = echo_request->length;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        memcpy(point,echo_request->data, echo_request->length);
        point += echo_request->length;
        (*length) += echo_request->length;
        
        
        break;
      case CHARGE_REUQEST:
        LOGI("CHARGE_REUQEST");
        handle_send_no_value_key(key_common, length, &point);
        break;
      case LED_OPERATION:
        LOGI("LED_OPERATION");
        test_led = (L2_command_test_led_request_t*)p;
        if(0 == test_led->have_mode) {
          handle_send_no_value_key(key_common, length, &point);
        } else {
          *point = LED_OPERATION;
          point ++;
          (*length) ++;
          
          bit16_length = 1;
          bit16_length = cpu_2_be16(bit16_length);
          memcpy(point, &bit16_length, 2);
          point += 2;
          (*length) += 2;
          
          *point = test_led->mode;
          point ++;
          (*length) ++;
          
        }
        break;
      case VIBRATE_OPERATION:
        LOGI("VIBRATE_OPERATION");
        handle_send_no_value_key(key_common, length, &point);
        break;
      case SN_WRITE:
        sn_write_request = (L2_command_test_sn_write_request_t*)p;
        LOGI("SN_WRITE");
        
        if((*length)+ 3 + 32 > L1_MTU) {
          free_L1_data(data);
          return NULL;
        }
        
        *point = SN_WRITE;
        point ++;
        (*length) ++;
        
        bit16_length = 32;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        memcpy(point,sn_write_request->sn, sizeof(sn_write_request->sn));
        point += sizeof(sn_write_request->sn);
        (*length) += sizeof(sn_write_request->sn);
        break;
      case SN_READ:
         LOGI("SN_READ");
         handle_send_no_value_key(key_common, length, &point);
        break;
      case TEST_FLAG_WRITE:
        test_flag_write_request = (L2_command_test_flag_write_request_t*)p;
        LOGI("TEST_FLAG_WRITE");
        
        *point = TEST_FLAG_WRITE;
        point ++;
        (*length) ++;
        
        bit16_length = 1;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        *point = test_flag_write_request->test_flag;
        point ++;
        (*length) ++;

        break;
      case TEST_FLAG_READ:
        LOGI("TEST_FLAG_READ");
        handle_send_no_value_key(key_common, length, &point);
        break;
      case SENSOR_DATA_REQUEST:
        LOGI("SENSOR_DATA_REQUEST");
        handle_send_no_value_key(key_common, length, &point);
        break;
      case TEST_MODE_ENTER:
        test_mode_enter_request = (L2_command_test_mode_enter_resquest_t*)p;
        LOGI("TEST_MODE_ENTER");
        
        if((*length)+ 3 + 32 > L1_MTU) {
          free_L1_data(data);
          return NULL;
        }
        
        *point = TEST_MODE_ENTER;
        point ++;
        (*length) ++;
        
        bit16_length = 32;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        memcpy(point,test_mode_enter_request->token, sizeof(test_mode_enter_request->token));
        point += sizeof(test_mode_enter_request->token);
        (*length) += sizeof(test_mode_enter_request->token);
        break;
      case TEST_MODE_EXIT:
        test_mode_exit_request = (L2_command_test_mode_exit_resquest_t*)p;
        LOGI("TEST_MODE_EXIT");
        
        if((*length)+ 3 + 32 > L1_MTU) {
          free_L1_data(data);
          return NULL;
        }
        
        *point = TEST_MODE_EXIT;
        point ++;
        (*length) ++;
        
        bit16_length = 32;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        memcpy(point,test_mode_exit_request->token, sizeof(test_mode_exit_request->token));
        point += sizeof(test_mode_exit_request->token);
        (*length) += sizeof(test_mode_exit_request->token);
        break;
        
      case MOTOR_BURN_IN_TEST:
        test_motor_burn_in = (L2_command_test_motor_burn_in_t*)p;
        LOGI("MOTOR_BURN_IN_TEST");
        
        if((*length)+ 3 + 1 > L1_MTU) {
          free_L1_data(data);
          return NULL;
        }
        
        *point = MOTOR_BURN_IN_TEST;
        point ++;
        (*length) ++;
        
        bit16_length = 1;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        *point = test_motor_burn_in->enable;
        point ++;
        (*length) ++;
        break;
        
      case LED_BURN_IN_TEST:
        test_led_burn_in = (L2_command_test_led_burn_in_t*)p;
        LOGI("LED_BURN_IN_TEST");
        
        if((*length)+ 3 + 1 > L1_MTU) {
          free_L1_data(data);
          return NULL;
        }
        
        *point = LED_BURN_IN_TEST;
        point ++;
        (*length) ++;
        
        bit16_length = 1;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        *point = test_led_burn_in->enable;
        point ++;
        (*length) ++;
        break;

        
      default:
        LOGE("send_factory_test_command key not support:%d",key_common->key);
        assert(0);
        break;
    }
    
  }
  
  return data;
}


unsigned char * send_remote_control_command(L2_command_t *command,uint16_t * length) {
  unsigned char *data;
  unsigned char *point;
  struct list_head *p;
  uint16_t bit16_length;
  
  LOGI("send_remote_control_command");
  
  data = handle_send_command_header(command,length,&point);
  
  if(NULL == data) {
    length = 0;
    return NULL;
  }
  
  list_for_each(p, &(command->key_list)) {
    L2_key_common_t *key_common;
    L2_command_remote_control_camera_state_request_t *camera_state_request;
    
    key_common = (L2_key_common_t*)p;
    
    switch(key_common->key) {
      case CAMERA_APP_STATE_REQUEST:
        camera_state_request = (L2_command_remote_control_camera_state_request_t*)p;
        LOGI("CAMERA_APP_STATE_REQUEST");
        
        *point = CAMERA_APP_STATE_REQUEST;
        point ++;
        (*length) ++;
        
        bit16_length = 1;
        bit16_length = cpu_2_be16(bit16_length);
        memcpy(point, &bit16_length, 2);
        point += 2;
        (*length) += 2;
        
        *point = camera_state_request->state;
        point ++;
        (*length) ++;
        break;
      default:
        LOGE("send_remote_control_command key not support:%d",key_common->key);
        assert(0);
        break;
    }
    
  }
  
  return data;
}



int send_L2(L2_command_t *command) {
  int seq_id;
  unsigned char *data = NULL;
  uint16_t length;


  LOGI("send_L2");
  
  if(0 == have_been_init) {
    LOGE("L2 have not been init");
    return -1;
  }
  
  lock_l1_implement();
  
  if(NULL == command) {
    LOGE("send_L2 command is NULL");
    unlock_l1_implement();
    return -1;
  }
  
  switch(command->header.command_id) {
    case HEALTH_BLE_COMMAND_ROM_UPATE:
        data = send_ota_command(command,&length);
        break;
        break;
    case HEALTH_BLE_COMMAND_SETTING:
        data = send_setting_command(command,&length);
        break;
    case HEALTH_BLE_COMMAND_BIND:
        data = send_bind_command(command,&length);
        break;
    case HEALTH_BLE_COMMAND_ALARM:
        data = send_alarm_command(command,&length);
        break;
    case HEALTH_BLE_COMMAND_SPORT_DATA:
        data = send_sport_data_command(command,&length);
        break;
    case HEALTH_BLE_COMMAND_FACTORY_TEST:
      data = send_factory_test_command(command,&length);
      break;
    case HEALTH_BLE_COMMAND_REMOTE_CONTROL:
      data = send_remote_control_command(command,&length);
      break;
    default:
        
        LOGE("send_L2 command command_id not support:%d",command->header.command_id);
        assert(0);
        break;
  }
  

  if(NULL == data) {
    unlock_l1_implement();
    return -1;
  }
  
  seq_id = send_L1(length,data);
  
  LOGI("call send_L1 seq_id:%d",seq_id);
  
  free_L1_data(data);
  
  unlock_l1_implement();
  return seq_id;
}

void on_timer_fire_l2() {
//  LOGI("on_timer_fire_l2" );
  
  if(0 == have_been_init) {
    LOGE("L1 have not been init");
    return;
  }
  
  lock_l1_implement();
  
  on_timer_fire_l1_p();
  
  unlock_l1_implement();
}


void create_timer_l1_implement(on_timer_fire_L1 timer_fire,int second) {
  LOGI("create_timer_l1_implement");
  if(NULL == create_timer_l2) {
    LOGE("create_timer_l2 is NULL");
    assert(0);
    return;
  }
  
  on_timer_fire_l1_p = timer_fire;
  
  create_timer_l2( on_timer_fire_l2, second);
  
}

void stop_timer_l1_implement() {
  LOGI("stop_timer_l1_implement");
  if(NULL == stop_timer_l2) {
    LOGI("stop_timer_l2 is NULL");
    assert(0);
    return;
  }
  
  stop_timer_l2();
}

void need_reset_l1_implement() {
  LOGI("need_reset_l1_implement");
  if(NULL == need_reset_l2) {
    LOGI("need_reset_l2 is NULL");
    assert(0);
    return;
  }
  
  need_reset_l2();
}

int init_health_ble_L2(recv_cb_L2 recv_cb, send_cb_L2 send_cb,create_timer_L2 create_timer, stop_timer_L2 stop_timer,lock_stack_L2 lock_l2, unlock_stack_L2 unlock_l2,need_reset_L2 need_reset )
{
  LOGI("init_health_ble_L2");
  
  int ret;
  
  if (NULL == recv_cb || NULL == send_cb || NULL == create_timer) {
    return -1;
  }
  
  recv_callback_l2 = recv_cb;
  send_callback_l2 = send_cb;
  
  create_timer_l2 = create_timer;
  stop_timer_l2 = stop_timer;
  
  lock_stack_l2 = lock_l2;
  unlock_stack_l2 = unlock_l2;
  
  need_reset_l2 = need_reset;
  
  ret = init_health_ble_L1(recv_cb_L1_implement, send_cb_L1_implement,create_timer_l1_implement,stop_timer_l1_implement,lock_l1_implement,unlock_l1_implement,need_reset_l1_implement);
  
  if(0 == ret) {
    have_been_init = 1;
  } else {
    LOGE("call init_health_ble_L1 error");
    recv_callback_l2 = NULL;
    send_callback_l2 = NULL;
    
    create_timer_l2 = NULL;
    stop_timer_l2 = NULL;
    
    lock_stack_l2 = NULL;
    unlock_stack_l2 = NULL;
    
    need_reset_l2 = NULL;
    
  }
  
  return ret;
}

int finalize_health_ble_L2() {
  LOGI("finalize_health_ble_L2");
  int ret = 0;
  
  lock_l1_implement();
  
  if(!have_been_init) {
    return -1;
  }
  
  ret = finalize_health_ble_L1();
  
  have_been_init = 0;
  
  recv_callback_l2 = NULL;
  send_callback_l2 = NULL;
  
  create_timer_l2 = NULL;
  stop_timer_l2 = NULL;
  
  lock_stack_l2 = NULL;
  
  unlock_l1_implement();
  
  unlock_stack_l2 = NULL;
  
  need_reset_l2 = NULL;
  
  return ret;
}







