#ifndef _HEALTH_BLE_STACK_L0_H_
#define _HEALTH_BLE_STACK_L0_H_

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif
  
#define SELF_THROUGH_TEST 0
  
#define L0_MTU      20

typedef struct health_ble_data_L0 {
  uint16_t      length;
  char *        data;
} health_ble_data_L0_t;

typedef void(*recv_cb_L0)(health_ble_data_L0_t * const data, int status_code);
typedef void(*send_cb_L0)(uint16_t seq_id, int status_code);
typedef int(*write_data_L0_to_ble)(char * const data, size_t length, uint16_t seq_id);

int init_health_ble_L0(recv_cb_L0 const recv_cb, send_cb_L0 const send_cb);

int send_L0(char * const data, size_t length, uint16_t seq_id);


int getL1CallBacks(recv_cb_L0 * const recv_cb, send_cb_L0 * const send_cb, const write_data_L0_to_ble  call_send_data);
  
  
#ifdef __cplusplus
}
#endif
#endif
