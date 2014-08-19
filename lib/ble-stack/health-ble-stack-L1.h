 #ifndef _HEALTH_BLE_STACK_L1_H_
 #define _HEALTH_BLE_STACK_L1_H_

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif
  
  #define L1_MTU    512
  
  typedef void(*recv_cb_L1)(uint16_t length,unsigned char *data);
  typedef void(*send_cb_L1)(uint16_t seq_id, int status_code);
  typedef void(*on_timer_fire_L1)();
  typedef void(*create_timer_L1)(on_timer_fire_L1 timer_fire,int second);
  typedef void(*stop_timer_L1)();
  
  typedef void(*lock_stack_L1)();
  
  typedef void(*unlock_stack_L1)();
  
  typedef void(*need_reset_L1)();
  
  
  /*
   *  func name: init_health_ble_L1, call this function to register callback
   *  param:
   *    recv_cb, callback of received data
   *    send_cb, callback of sent data
   *    create_timer, call by L1 implement to create timer used by L1
   *    lock_l1,unlock_l1 call by L1 implement to support cocurrent condition
   */
  int init_health_ble_L1(recv_cb_L1 recv_cb, send_cb_L1 send_cb,create_timer_L1 create_timer, stop_timer_L1 stop_timer,lock_stack_L1 lock_l1,unlock_stack_L1 unlock_l1,need_reset_L1 need_reset);
  
  /* every time the connection lost, should call finalize_health_ble_L1
   *
   *
   */
  int finalize_health_ble_L1();
  
  /*
   *  send_L1, call this func to send Data, Asynchronous function
   * return value: -1 error, >= 0 seq_id.
   */
  int send_L1(uint16_t length,char *data);
  
  void uint16_to_byte(unsigned char * const byte_array,const uint16_t val);
  void byte_to_uint16(uint16_t * const val, unsigned char * const byte_array);
  
#ifdef __cplusplus
}
#endif

#endif
