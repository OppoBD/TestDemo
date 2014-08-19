 #ifndef _HEALTH_BLE_STACK_L2_H_
 #define _HEALTH_BLE_STACK_L2_H_

#include <stdint.h>
#include "list.h"

#ifdef __cplusplus
extern "C" {
#endif
  
  typedef enum L2_command_id {
    HEALTH_BLE_COMMAND_ROM_UPATE = 0x01,
    HEALTH_BLE_COMMAND_SETTING = 0x02,
    HEALTH_BLE_COMMAND_BIND = 0x03,
    HEALTH_BLE_COMMAND_ALARM = 0x04,
    HEALTH_BLE_COMMAND_SPORT_DATA = 0x05,
    HEALTH_BLE_COMMAND_FACTORY_TEST = 0x06,
    HEALTH_BLE_COMMAND_REMOTE_CONTROL = 0x07
  }L2_command_id_t;
  
  enum L2_command_ota_keys {
    OTA_ENTER_OTA_MODE = 0x01,
    OTA_ENTER_OTA_MODE_RESPONSE = 0x02
  };

  enum L2_command_setting_keys {
    SETTING_TIME = 0x01,
    SETTING_ALARM = 0x02,
    SETTING_GET_ALARM_REQUEST = 0x03,
    SETTING_GET_ALARM_RESPONSE = 0x04,
    SETTING_SPORT_TARGET = 0x05,
    SETTING_USER_PROFILE = 0x10,
    SETTING_LINK_LOST_REQUEST = 0x20,
    SETTING_STILL_ALARM = 0x21,
    SETTING_LEFT_OR_RIGHT = 0x22,
    SETTING_OS = 0x23,
    SETTING_CONTACTS_WHITE_LIST = 0x24,
    SETTING_CALLING_NOTIFICATION_SWITCH = 0x25
  };

  enum L2_command_bind_keys {
    BIND_REQUEST = 0x01,
    BIND_RESPONSE = 0x02,
    LOGIN_REQUEST = 0x03,
    LOGIN_RESPONSE = 0x04,
    UNBIND_REQUEST = 0x05
  };

  enum L2_command_alarm_keys {
    PHONE_COMMING = 0x01,
    PHONE_ANSWER = 0x02,
    PHONE_DENY = 0x03
  };


  enum L2_command_sport_data_keys {
    DATA_REQUEST = 0x01,
    DATA_RESPONSE_SPORT = 0x02,
    DATA_RESPONSE_SLEEP = 0x03,
    DATA_RESPONSE_MORE = 0x04,
    DATA_RESPONSE_SLEEP_SETTING = 0x05,
    DATA_SYNC_SETTING = 0x06,
    DATA_SYNC_START = 0x07,
    DATA_SYNC_END = 0x08,
    DATA_DAILY_DATA_SYNC = 0x09
  };
  
  enum L2_command_factory_test_keys {
    ECHO_REQUEST = 0x01,
    ECHO_RESPONSE = 0x02,
    CHARGE_REUQEST = 0x03,
    CHARGE_RESPONSE = 0x04,
    LED_OPERATION = 0x05,
    VIBRATE_OPERATION = 0x06,
    SN_WRITE = 0x07,
    SN_READ = 0x08,
    SN_RESPONSE = 0x09,
    TEST_FLAG_WRITE = 0x0A,
    TEST_FLAG_READ = 0x0B,
    TEST_FLAG_RESPONSE = 0x0C,
    SENSOR_DATA_REQUEST = 0x0D,
    SENSOR_DATA_RESPONSE = 0x0E,
    TEST_MODE_ENTER = 0x10,
    TEST_MODE_EXIT = 0x11,
    BUTTON_TEST = 0x21,
    MOTOR_BURN_IN_TEST = 0x31,
    LED_BURN_IN_TEST = 0x32
    
  };
  
  enum L2_command_remote_control_keys {
    CAMERA_TAKE_PICTURE = 0x01,
    SINGLE_CLICK = 0x02,
    DOUBLE_CLICK = 0x03,
    CAMERA_APP_STATE_REQUEST = 0x11
  };
  
  typedef struct L2_key_common {
    struct list_head list;
    unsigned char key;
  }L2_key_common_t;
  
  typedef struct L2_command_header {
    unsigned char command_id;
    unsigned char command_version;
  }L2_command_header_t;
  
  typedef struct L2_command {
    L2_command_header_t header;
    struct list_head key_list;
  }L2_command_t;
  
  
  typedef struct L2_command_ota_enter_ota_mode_request {
    L2_key_common_t common;
  } L2_command_ota_enter_ota_mode_request_t;
  
  
  typedef struct L2_command_ota_enter_ota_mode_response {
    L2_key_common_t common;
    unsigned char status_code;
    unsigned char error_code;
  } L2_command_ota_enter_ota_mode_response_t;

  typedef struct L2_command_setting_time {
    L2_key_common_t common;
    
    unsigned char year; //0-63
    unsigned char month; //1-12
    unsigned char day; //1-31
    unsigned char hour; //0-23
    unsigned char minute;//0-59
    unsigned char second; //0-59
  } L2_command_setting_time_t;
  
  typedef struct L2_command_setting_alarm_item {
    struct list_head list;
    
    unsigned char year; //0-63
    unsigned char month; //1-12
    unsigned char day; //1-31
    unsigned char hour; //0-23
    unsigned char minute;//0-59
    unsigned char alarm_id;
    unsigned char Mon;
    unsigned char Tue;
    unsigned char Wed;
    unsigned char Thu;
    unsigned char Fri;
    unsigned char Sat;
    unsigned char Sun;
  } L2_command_setting_alarm_item_t;
  
  typedef struct L2_command_setting_alarm {
    L2_key_common_t common;
    struct list_head alarm_list;
  } L2_command_setting_alarm_t;
  
  typedef struct L2_command_setting_get_alarm_request {
    L2_key_common_t common;
  } L2_command_setting_get_alarm_request_t;
  
  typedef struct L2_command_setting_get_alarm_response {
    L2_key_common_t common;
    struct list_head alarm_list;
  } L2_command_setting_get_alarm_response_t;
  
  typedef struct L2_command_setting_sport_target {
    L2_key_common_t common;
    uint32_t target;
  } L2_command_setting_sport_target_t;
  
  /**
   @param weight body weight in kg.
   @param height body height in cm.
   */
  typedef struct L2_command_setting_user_profile {
    L2_key_common_t common;
    unsigned char ismale;
    unsigned char age;
    uint16_t height;
    uint16_t weight;
  } L2_command_setting_user_profile_t;
  
  typedef struct L2_command_setting_link_lost_request {
    L2_key_common_t common;
    unsigned char alert_level;
  } L2_command_setting_link_lost_request_t;
  
  typedef struct L2_command_setting_still_alarm_request {
    L2_key_common_t common;
    unsigned char reserved;
    unsigned char enable;
    uint16_t steps;
    unsigned char minutes;
    unsigned char start_hour;
    unsigned char end_hour;
    unsigned char day_flag;
  } L2_command_setting_still_alarm_request_t;
  
  
  typedef enum  {
    SETTING_LEFT_HAND = 0x01,
    SETTING_RIGHT_HAND = 0x02
  }setting_left_or_right_hand_value_t;
  
  typedef struct L2_command_setting_left_or_right_hand {
    L2_key_common_t common;
    unsigned char value;
  }L2_command_setting_left_or_right_hand_t;
  
  typedef enum  {
    SETTING_OS_IOS = 0x01,
    SETTING_OS_ANDROID = 0x02,
    SETTING_OS_UNKNOW = 0xFF
  }setting_os_value_t;
  
  typedef struct L2_command_setting_os {
    L2_key_common_t common;
    setting_os_value_t value;
    unsigned char reserved;
  }L2_command_setting_os_t;

  typedef struct L2_command_setting_calling_contacts_white_list_item {
    struct list_head list;
    char value[20];
  }L2_command_setting_calling_contacts_white_list_item_t;

  typedef enum  {
    SETTING_CONTACT_ADD = 0x01,
    SETTING_CONTACT_REPLACE = 0x02
  }setting_contact_action_t;

  typedef struct L2_command_setting_calling_contacts_white_list {
    L2_key_common_t common;
    setting_contact_action_t action;
    struct list_head contacts;
  }L2_command_setting_calling_contacts_white_list_t;
  
  typedef struct L2_command_setting_callling_notification_switch {
    L2_key_common_t common;
    unsigned char value ;
  }L2_command_setting_calling_notification_switch_t;

  typedef struct L2_command_bind_bind_request {
    L2_key_common_t common;
    char userid[32];
  }L2_command_bind_bind_request_t;
  
  typedef struct L2_command_bind_unbind_request {
    L2_key_common_t common;
    unsigned char reserved;
  }L2_command_bind_unbind_request_t;

  typedef struct L2_command_bind_bind_response {
    L2_key_common_t common;
    unsigned char status_code;
  }L2_command_bind_bind_response_t;
  
  typedef struct L2_command_bind_login_request {
    L2_key_common_t common;
    char userid[32];
  }L2_command_bind_login_request_t;
  
  typedef struct L2_command_bind_login_response {
    L2_key_common_t common;
    unsigned char status_code;
  }L2_command_bind_login_response_t;
  
  typedef struct L2_command_alarm_phone_call {
    L2_key_common_t common;
  }L2_command_alarm_phone_call_t;
  
  typedef struct L2_command_alarm_phone_answer {
    L2_key_common_t common;
  }L2_command_alarm_phone_answer_t;
  
  typedef struct L2_command_alarm_phone_deny {
    L2_key_common_t common;
  }L2_command_alarm_phone_deny_t;


  typedef struct L2_command_sport_data_request {
    L2_key_common_t common;
  }L2_command_sport_data_request_t;

  typedef struct L2_sleep_item {
    struct list_head list;
    
    uint16_t minute;
    unsigned char mode;
  }L2_sleep_item_t;


  typedef struct L2_command_sport_data_response_sleep {
    L2_key_common_t common;
    
    unsigned char year; //0-63;
    unsigned char month; //1-12
    unsigned char day; //1-31
    
    struct list_head sleep_list;
  }L2_command_sport_data_response_sleep_t;


  typedef struct L2_sport_item {
    struct list_head list;
    
    uint16_t offset;
    unsigned char mode;
    uint16_t steps;
    unsigned char active_time;
    uint16_t calory;
    uint16_t distance;
  }L2_sport_item_t;

  typedef struct L2_command_sport_data_response_sport {
    L2_key_common_t common;
    
    unsigned char year; //0-63;
    unsigned char month; //1-12
    unsigned char day; //1-31
    
    struct list_head sport_list;
  }L2_command_sport_data_response_sport_t;


  typedef struct L2_command_sport_data_response_more {
     L2_key_common_t common;
  }L2_command_sport_data_response_more_t;


  
  typedef struct L2_sleep_setting_item {
    struct list_head list;
    
    uint16_t minute; //0 - 24*60-1
    unsigned char mode;
  }L2_sleep_setting_item_t;
  
  
  typedef struct L2_command_sport_data_response_sleep_setting {
    L2_key_common_t common;
    
    unsigned char year; //0-63;
    unsigned char month; //1-12
    unsigned char day; //1-31
    
    struct list_head sleep_setting_list;
  }L2_command_sport_data_response_sleep_setting_t;
  
  typedef struct L2_command_sport_data_sync_setting {
    L2_key_common_t common;
    unsigned char enable;
  }L2_command_sport_data_sync_setting_t;
  
  typedef struct L2_command_sport_data_sync_start{
    L2_key_common_t common;
  }L2_command_sport_data_sync_start_t;
  
  typedef struct L2_command_sport_data_sync_end{
    L2_key_common_t common;
  }L2_command_sport_data_sync_end_t;
  
  typedef struct L2_command_sport_daily_data_sync{
    L2_key_common_t common;
    uint32_t daily_step;
    uint32_t daily_distance; //in m
    uint32_t daily_calory; //in 1/1000 cal
  }L2_command_sport_daily_data_sync_t;

  
  typedef struct L2_command_test_echo_request {
    L2_key_common_t common;
    uint16_t length;
    char *data;
  }L2_command_test_echo_request_t;
  
  typedef struct L2_command_test_echo_response {
    L2_key_common_t common;
    uint16_t length;
    char *data;
  }L2_command_test_echo_response_t;
  
  typedef struct L2_command_test_charge_request {
    L2_key_common_t common;
  }L2_command_test_charge_request_t;
  
  typedef struct L2_command_test_charge_response {
    L2_key_common_t common;
    uint16_t voltage;
  }L2_command_test_charge_response_t;
  
  typedef struct L2_command_test_led_request {
    L2_key_common_t common;
    unsigned char have_mode; //0 no mode value,so key's value should be empty
    unsigned char mode;
  }L2_command_test_led_request_t;
  
  typedef struct L2_command_test_vibrate_request {
    L2_key_common_t common;
  }L2_command_test_vibrate_request_t;
  
  typedef struct L2_command_test_sn_write_request {
    L2_key_common_t common;
    char sn[32];
  }L2_command_test_sn_write_request_t;
  
  typedef struct L2_command_test_sn_read_request {
    L2_key_common_t common;
  }L2_command_test_sn_read_request_t;
  
  typedef struct L2_command_test_sn_read_response {
    L2_key_common_t common;
    char sn[32];
  }L2_command_test_sn_read_response_t;
  
  typedef struct L2_command_test_flag_write_request {
    L2_key_common_t common;
    char test_flag;
  }L2_command_test_flag_write_request_t;
  
  typedef struct L2_command_test_flag_read_request {
    L2_key_common_t common;
  }L2_command_test_flag_read_request_t;
  
  typedef struct L2_command_test_flag_read_response {
    L2_key_common_t common;
    char test_flag;
  }L2_command_test_flag_read_response_t;
  
  typedef struct L2_command_test_sensor_read_request {
    L2_key_common_t common;
  }L2_command_test_sensor_read_request_t;
  
  typedef struct L2_command_test_sensor_read_response {
    L2_key_common_t common;
    uint16_t x_axis;
    uint16_t y_axis;
    uint16_t z_axis;
  }L2_command_test_sensor_read_response_t;
  
  typedef struct L2_command_test_mode_enter_resquest {
    L2_key_common_t common;
    char token[32];
  }L2_command_test_mode_enter_resquest_t;

  typedef struct L2_command_test_mode_exit_resquest {
    L2_key_common_t common;
    char token[32];
  }L2_command_test_mode_exit_resquest_t;
  
  typedef struct L2_command_test_button {
    L2_key_common_t common;
    uint8_t  code;
    uint8_t  button_id;
    uint16_t reserved;
    uint32_t timestamp;
  }L2_command_test_button_t;
  
  
  typedef enum  {
    MOTOR_BURN_IN_TEST_ENABLED = 0x01,
    MOTOR_BURN_IN_TEST_DISABLED = 0x02
  }test_motor_burn_in_value_t;
  
  typedef struct L2_command_test_motor_burn_in {
    L2_key_common_t common;
    unsigned char enable;
  }L2_command_test_motor_burn_in_t;
  
  typedef enum  {
    LED_BURN_IN_TEST_ENABLED = 0x01,
    LED_BURN_IN_TEST_DISABLED = 0x02
  }test_led_burn_in_value_t;
  
  typedef struct L2_command_test_led_burn_in {
    L2_key_common_t common;
    unsigned char enable;
  }L2_command_test_led_burn_in_t;
  
  
  typedef struct L2_command_remote_control_camera_take_picture {
    L2_key_common_t common;
  }L2_command_remote_control_camera_take_picture_t;
  
  typedef struct L2_command_remote_control_single_click {
    L2_key_common_t common;
  }L2_command_remote_control_single_click_t;
  
  typedef struct L2_command_remote_control_double_click {
    L2_key_common_t common;
  }L2_command_remote_control_double_click_t;
  
  typedef struct L2_command_remote_control_camera_state_request {
    L2_key_common_t common;
    unsigned char state;
  }L2_command_remote_control_camera_state_request_t;
  
  
  typedef void(*recv_cb_L2)(L2_command_t *command);
  typedef void(*send_cb_L2)(uint16_t seq_id, int status_code);
  typedef void(*on_timer_fire_L2)();
  typedef void(*create_timer_L2)(on_timer_fire_L2 timer_fire,int second);
  typedef void(*stop_timer_L2)();
  
  typedef void(*lock_stack_L2)();
  
  typedef void(*unlock_stack_L2)();
  
  typedef void(*need_reset_L2)();
  
  /**
   *  func name: init_health_ble_L2, call this function to register callback
   *  param:
   *    recv_cb, callback of received data
   *    send_cb, callback of sent data
   *    lock_l2,unlock_l2 call by L2 implement to support cocurrent condition
   */
  int init_health_ble_L2(recv_cb_L2 recv_cb, send_cb_L2 send_cb,create_timer_L2 create_timer, stop_timer_L2 stop_timer,lock_stack_L2 lock_l2,unlock_stack_L2 unlock_l2,need_reset_L2 need_reset);

  /** every time the connection lost, should call finalize_health_ble_L2
   *
   *
   */
  int finalize_health_ble_L2();
  
  /**
   *  send_L2, call this func to send Data, Asynchronous function
   * return value: -1 error, > 0 valid seq_id.
   */
  int send_L2(L2_command_t *command);
    
   //help functions
  void free_command(L2_command_t *command);
  
  L2_command_t * copy_command(L2_command_t *command);
  
  void free_key_common(unsigned char command_id,L2_key_common_t *key_common);
  
#ifdef __cplusplus
}
#endif

#endif
