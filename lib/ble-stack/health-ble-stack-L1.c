//
//  health-ble-stack-L1.c
//  baiduhealth
//
//  Created by Chen Xiaobin on 9/9/13.
//  Copyright (c) 2013 Baidu. All rights reserved.
//

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "health-ble-stack-L0.h"
#include "health-ble-stack-L1.h"
#include "list.h"
#include "bit-order.h"
#include "crc16.h"

#define L1_DEBUG 1

#if (ANDROID && L1_DEBUG)

#include <android/log.h>
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "health-ble-stack-L1", __VA_ARGS__))                                                                              
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "health-ble-stack-L1", __VA_ARGS__))

#define DUMP_L1_DATA(length,data)  \
  do {            \
    int i = 0;  \
    LOGI("dump_L1_data");   \
    if(NULL != (data)) {   \
      for(i=0; i<(length); i++) {  \
        LOGI("data[%d]:0x%02x",i,(data)[i]);   \
      }   \
    }   \
  } while(0)

#else 
#if DEBUG

#include "health-ble-stack_c_bridge.h"
static char buffer_string[1000];
#define LOGI(...) do {sprintf(buffer_string, __VA_ARGS__);print_c_log_info(buffer_string);} while(0)
#define LOGE(...) do {sprintf(buffer_string, __VA_ARGS__);print_c_log_warning(buffer_string);} while(0)
#define DUMP_L1_DATA(length,data)  \
do {            \
int i = 0;  \
LOGI("dump_L1_data");   \
if(NULL != (data)) {   \
for(i=0; i<(length); i++) {  \
LOGI("data[%d]:0x%x",i,(data)[i]);   \
}   \
}   \
} while(0)
#else
#define LOGI(...) do {} while(0)
#define LOGE(...) do {} while(0)
#define DUMP_L1_DATA(length,data) do {} while(0)
#endif
#endif 


#define L1_MAGIC 0xAB
#define L1_VERSION 0x00

enum L1_item_status {
  L1_ITEM_IN_POOL = 0x01,
  L1_ITEM_IN_SENDING = 0x02,
  L1_ITEM_HAVE_SENDED = 0x04,
  L1_ITEM_HAVE_ACK_OK = 0x08,
  L1_ITEM_HAVE_ACK_ERROR = 0x10
};

enum L0_item_status {
  L0_ITEM_IN_POOL = 0x01,
  L0_ITEM_IN_SENDING = 0x02,
  L0_ITEM_HAVE_SENDED = 0x04
};

typedef struct health_ble_L1_header {
    unsigned char magic;
    unsigned char version_flags;
    uint16_t payload_length;
    uint16_t crc16;
    uint16_t sequence_id;
} health_ble_L1_header_t;

typedef struct L1_pool_item {
  struct list_head list;
  long send_time_count;
  uint16_t length;
  unsigned char tag;
  unsigned char status;
  char *data;
  struct list_head L0_mtu_list;
}L1_pool_item_t;


typedef struct L0_mtu_item {
  struct list_head list;
  unsigned char length;
  unsigned char tag;
  unsigned char status;
  unsigned char retry;
  char *data;
}L0_mtu_item_t;


#if (L1_VERSION == 0x00)

#define L0_MTU_RETRY_MAX      2

#endif

#define L1_POOL_MAX_SIZE 10
#define L1_TIME_FIRE_INTERVAL 1
#define L1_ACK_TIME_EXPIRE 10
#define WAIT_ACK_L1_ITEM_MAX 1

#define MAX_ERROR_TORRENT 3

long l1_time_count = 0;

struct list_head L1_pool_list;

struct list_head L1_ack_pool_list;

struct list_head L1_have_send_list;

L1_pool_item_t *sending_item;

unsigned char tag_L1 = 0;

uint16_t block_L1_size = 0;

uint16_t pool_item_size = 0;

recv_cb_L1 recv_callback_l1 = NULL;
send_cb_L1 send_callback_l1 = NULL;
create_timer_L1 create_timer_l1 = NULL;
stop_timer_L1 stop_timer_l1 = NULL;

lock_stack_L1 lock_stack_l1 = NULL;
unlock_stack_L1 unlock_stack_l1 = NULL;

need_reset_L1 need_reset_l1 = NULL;


uint16_t L1_have_receive_length;

char L1_recv_buffer[L1_MTU];

char L1_data_buffer[L1_MTU];

static unsigned char have_been_init = 0;


unsigned char error_happen_count = 0;


static void dump_L1_header(health_ble_L1_header_t *l1_header) {
  LOGI("dump_L1_header");
  
  LOGI("magic:0x%x",l1_header->magic);
  LOGI("version_flags:0x%x",l1_header->version_flags);
  LOGI("payload_length:0x%x",l1_header->payload_length);
  LOGI("crc16:0x%x",l1_header->crc16);
  LOGI("sequence_id:0x%x",l1_header->sequence_id);
}



static inline int is_ack_packet(health_ble_L1_header_t *header) {
  assert(NULL != header);
  if(header->version_flags & 0x10) {
    return 1;
  } else {
    return 0;
  }
}

static inline int is_error_flag_set(health_ble_L1_header_t *header) {
  assert(NULL != header);
  if(header->version_flags & 0x20) {
    return 1;
  } else {
    return 0;
  }
}

static inline void L1_set_ack_flag(health_ble_L1_header_t *header) {
  assert(NULL != header);
  
  header->version_flags = (header->version_flags | 0x10);
  
  return;
}

static inline void L1_set_error_flag(health_ble_L1_header_t *header) {
  assert(NULL != header);
  
  header->version_flags = (header->version_flags | 0x20);
  
  return;
}

static inline void L1_set_version(health_ble_L1_header_t *header,unsigned char version) {
  assert(NULL != header);
  
  header->version_flags = (header->version_flags & 0xf0)|(version & 0xf);
  
  return;
}

static inline unsigned char L1_get_version(health_ble_L1_header_t *header) {
  assert(NULL != header);
  
  return (header->version_flags & 0x0f);
}

void free_L1_pool_item(L1_pool_item_t *pool_item){
  struct list_head *p,*n;
  if(NULL == pool_item) {
    return;
  }
  
  list_for_each_safe(p,n, &(pool_item->L0_mtu_list)){
    L0_mtu_item_t *l0_item;
    list_del(p);
    l0_item = (L0_mtu_item_t*)p;
    free(l0_item);
  }
  
  if(NULL != pool_item->data) {
    free(pool_item->data);
  }
  
  free(pool_item);
  

}

void uint16_to_byte(unsigned char * const byte_array,const uint16_t val) {
  *byte_array = val>>8;
  *(byte_array + 1) = val;
}

void byte_to_uint16(uint16_t * const val, unsigned char * const byte_array) {
  *val = *byte_array;
  *val = *val<<8;
  *val = (*val & 0xff00);
  *val |= * (byte_array + 1);
}

void lock_l0_implement() {
//  LOGI("lock_l0_implement");
  if(NULL == lock_stack_l1) {
    LOGE("lock_stack_l1 is NULL");
    assert(0);
    return;
  }
  
  lock_stack_l1();
//  LOGI("lock_l0_implement exit");
  
}

void unlock_l0_implement() {
//  LOGI("unlock_l0_implement");
  if(NULL == unlock_stack_l1) {
    LOGE("unlock_stack_l1 is NULL");
    assert(0);
    return;
  }
  
  unlock_stack_l1();
//  LOGI("unlock_l0_implement exit");
}

int send_l1_item(L1_pool_item_t *l1_item);

int send_l0_item(L0_mtu_item_t *l0_item) {
  int ret;
  
  LOGI("send_l0_item");
  if(NULL == l0_item) {
    LOGI("send_l0_item l0_item is NULL");
    assert(0);
    return -1;
  }
  
  LOGI("l0 tag:0x%x",l0_item->tag);
  
  l0_item->status |= L0_ITEM_IN_SENDING;
  
  ret = -1;
  while(ret < 0 && l0_item->retry < L0_MTU_RETRY_MAX) {
    ret = send_L0(l0_item->data, l0_item->length, l0_item->tag);
    l0_item->retry ++;
  }
  
  LOGI("send_L0 ret tag:0x%x",ret);
  
  if(ret) {
  
    error_happen_count ++;
  
    if(error_happen_count > MAX_ERROR_TORRENT && NULL != need_reset_l1) {
      LOGI("need reset the stack");
      need_reset_l1();
    }
  }

  return ret;
}

int send_l1_item(L1_pool_item_t *l1_item) {
  int ret;
  
  LOGI("send_l1_item");
  if(NULL == l1_item) {
    LOGI("send_l1_item l1_item is NULL");
    assert(0);
    return -1;
  }
  
  LOGI("l1 tag:0x%x", l1_item->tag);
  l1_item->status = L1_ITEM_IN_SENDING;
  
  L0_mtu_item_t *l0_item = (L0_mtu_item_t *)list_first(&(l1_item->L0_mtu_list));
  ret = send_l0_item(l0_item);
  
  return ret;
}

void send_l1_item_from_cb();

void send_l0_item_from_cb(L0_mtu_item_t *l0_item) {
  int ret;
//  struct list_head *p;
  
  LOGI("send_l0_item_from_cb");
  
  if(NULL == l0_item) {
    LOGE("l0_item is NULL");
    assert(0);
    return;
  }
  
  LOGI("l0 tag:0x%x", l0_item->tag);
  
  ret = send_l0_item(l0_item);
  
  if(ret < 0) {
    
    if(!is_ack_packet((health_ble_L1_header_t*)(sending_item->data))) {
      send_callback_l1(sending_item->tag, -1);
    }
    list_del(&(sending_item->list));
    free_L1_pool_item(sending_item);
    sending_item = NULL;


    send_l1_item_from_cb();
  }
}

void send_l1_item_from_cb() {
  int ret = -1;
  struct list_head *p,*n;//,*list;
  
//  LOGI("send_l1_item_from_cb");
  
  
  if(NULL != sending_item) {
    return;
  }
  
  
  list_for_each_safe(p,n, &L1_ack_pool_list) {
    sending_item = (L1_pool_item_t*)p;
    ret = send_l1_item(sending_item);
    if(0 == ret) {
      break;
    } else {
      list_del(&(sending_item->list));
      free_L1_pool_item(sending_item);
      sending_item = NULL;
    }
  }
  
  
  if(NULL == sending_item && list_size(&L1_have_send_list) < WAIT_ACK_L1_ITEM_MAX) {
    list_for_each_safe(p,n, &L1_pool_list) {
      sending_item = (L1_pool_item_t*)p;
      ret = send_l1_item(sending_item);
      if(0 == ret) {
        break;
      } else {
        send_callback_l1(sending_item->tag, -1);
        list_del(&(sending_item->list));
        free_L1_pool_item(sending_item);
        sending_item = NULL;
      }
    }
  }
}


static int send_data_with_header(health_ble_L1_header_t *header,uint16_t length,char *data,unsigned char is_from_cb,unsigned char is_ack) {
  uint16_t data_offset;
  unsigned char l0_tag;
  int ret;
  
  LOGI("send_data_with_header is_from_cb:%d is_ack:%d",is_from_cb,is_ack);
  
  dump_L1_header(header);
  
  if(pool_item_size >= L1_POOL_MAX_SIZE && !is_ack_packet(header)) {
    LOGE("pool_item_count reach to MAX and not ack packet" );
    return -1;
  }
  
  L1_pool_item_t *pool_item = (L1_pool_item_t *)malloc(sizeof(L1_pool_item_t));
  if(NULL == pool_item) {
    LOGE("malloc pool_item error" );
    return -1;
  }
  
  memset(pool_item,0,sizeof(L1_pool_item_t));
  
  INIT_LIST_HEAD(&(pool_item->L0_mtu_list));
  pool_item->tag = be16_2_cpu(header->sequence_id);
  
  if(is_ack) {
    list_add(&(pool_item->list), &L1_ack_pool_list);
  } else {
    list_add_tail(&(pool_item->list), &L1_pool_list);
  }
  pool_item->status |= L1_ITEM_IN_POOL;
  pool_item_size ++;
  
  
  pool_item->length = length + sizeof(health_ble_L1_header_t);
  pool_item->data = malloc(pool_item->length);
  
  if(NULL == pool_item->data) {
    LOGE("malloc pool_item->data error" );
    pool_item_size --;
    list_del(&(pool_item->list));
    free_L1_pool_item(pool_item);
    
    return -1;
  }
  
  memcpy((void*)(pool_item->data), (const void*)header, (unsigned long)sizeof(health_ble_L1_header_t));
  
  memcpy(pool_item->data + sizeof(health_ble_L1_header_t), data, length);
  
  DUMP_L1_DATA(pool_item->length,pool_item->data);
  
  data_offset = 0;
  l0_tag = 0;
  while(data_offset < pool_item->length) {
    LOGI("malloc new pool_item" );
    L0_mtu_item_t *l0_item = (L0_mtu_item_t*)malloc(sizeof(L0_mtu_item_t));
    if(NULL == l0_item) {
      LOGE("malloc pool_item->data error" );
      pool_item_size --;
      list_del(&(pool_item->list));
      free_L1_pool_item(pool_item);
      return -1;
    }
    memset(l0_item,0,sizeof(L0_mtu_item_t));
    list_add_tail(&(l0_item->list), &(pool_item->L0_mtu_list));
    l0_item->status |= L0_ITEM_IN_POOL;
    l0_item->tag = l0_tag;
    LOGI(" new pool_item l0 tag:%d", l0_item->tag);
    l0_item->retry = 0;
    l0_tag ++;
    l0_item->data = pool_item->data + data_offset;
    
    if(pool_item->length - data_offset > L0_MTU) {
      l0_item->length = L0_MTU;
    } else {
      l0_item->length = pool_item->length - data_offset;
    }
    data_offset += l0_item->length;
  }
  
  if (NULL == sending_item ) {   
    if(is_from_cb) {
      send_l1_item_from_cb();
      return 0;
    } else if(list_size(&L1_have_send_list) < WAIT_ACK_L1_ITEM_MAX) {
      LOGI("no sending L1_pool_item_t exist and have send is not exceed the limit so send it" );
      if(!list_empty(&L1_ack_pool_list)) {
        sending_item = (L1_pool_item_t*)list_first(&L1_ack_pool_list);
      } else {
        assert(pool_item == (L1_pool_item_t*)list_first(&L1_pool_list));
        sending_item = pool_item;
      }
      ret = send_l1_item(sending_item);
      if(ret < 0) {
        list_del(&(sending_item->list));
        free_L1_pool_item(sending_item);
        sending_item = NULL;
        return ret;
      } else {
        return pool_item->tag;
      }
    } else {
      return pool_item->tag;
    }
  } else {
    LOGI("l1 tag:0x%x", pool_item->tag);
    return pool_item->tag;
  }
  LOGE("should not reach this");
  assert(0);
}



void send_ack_packet(unsigned char err,uint16_t sequence_id) {
  LOGI("send_ack_packet" );
  
  health_ble_L1_header_t header;
  memset(&header,0,sizeof(header));
  header.magic = L1_MAGIC;
  L1_set_version(&header, L1_VERSION);
  L1_set_ack_flag(&header);
  if(err) {
    L1_set_error_flag(&header);
  }
  header.payload_length = cpu_2_be16(0);
  header.crc16 = cpu_2_be16(crc16(0, NULL, 0));
  header.sequence_id = sequence_id;
  header.sequence_id = cpu_2_be16(header.sequence_id);
  
  send_data_with_header(&header,0,NULL,1,1);
  
}


void L0_receive_data_cb(health_ble_data_L0_t *data, int staus_code)
{
  health_ble_L1_header_t *header;
  uint16_t crc;
  struct list_head *p,*n;
  L1_pool_item_t *pool_item = NULL;
  unsigned char find_match_l1_item = 0;
//  int ret;
  
  LOGI("L0_receive_data_cb ");
  
  if(0 == have_been_init) {
    LOGE("L1 have not been init");
    return;
  }
  
  lock_l0_implement();
  
  if(0 == have_been_init) {
    LOGE("L1 have not been init");
    return;
  }
  
  if(NULL == data || NULL == data->data) {
    LOGE("received_data_on_L0 data is NULL ");
    unlock_l0_implement();
    assert(0);
    return;
  }
  
  LOGI("L1_have_receive_length:%d data->length:%d",L1_have_receive_length,data->length);
  
  memcpy(L1_recv_buffer + L1_have_receive_length, data->data, data->length);
  
  L1_have_receive_length += data->length;
  
  if(L1_have_receive_length < sizeof(health_ble_L1_header_t)) {
    unlock_l0_implement();
    return;
  }
  
  LOGI("get L1 header ");
  
  header = (health_ble_L1_header_t*)(L1_recv_buffer);
  
  dump_L1_header(header);
  
  if(header->magic != L1_MAGIC) {
    LOGE("magic error");
    L1_have_receive_length = 0;
    assert(0 == L1_have_receive_length);
    block_L1_size = 0;
    
    error_happen_count ++;
    
    if(error_happen_count > MAX_ERROR_TORRENT && NULL != need_reset_l1) {
      LOGI("need reset the stack");
      need_reset_l1();
    }
    unlock_l0_implement();
    return;
  }
  
  block_L1_size = be16_2_cpu(header->payload_length);
  
  if(block_L1_size > L1_MTU - sizeof(health_ble_L1_header_t)) {
    LOGE("payload_length error");
    L1_have_receive_length = 0;
    assert(0 == L1_have_receive_length);
    block_L1_size = 0;
    
    error_happen_count ++;
    
    if(error_happen_count > MAX_ERROR_TORRENT && NULL != need_reset_l1) {
      LOGI("need reset the stack");
      need_reset_l1();
    }
    unlock_l0_implement();
    return;
  }
  
  
  if(L1_have_receive_length < block_L1_size + sizeof(health_ble_L1_header_t)) {
    unlock_l0_implement();
    return;
  }
  
  LOGI("receive total data of L1 packet ");
    
  if(is_ack_packet(header)) {
    LOGI("is a ack packet l1 tag:0x%x ",be16_2_cpu(header->sequence_id));
    list_for_each_safe(p, n,&L1_have_send_list) {
      pool_item = (L1_pool_item_t *)p;
      
      if(pool_item->tag == be16_2_cpu(header->sequence_id)) {
        find_match_l1_item = 1;
        break;
      }      
    }
    
    if(!find_match_l1_item) {
      LOGI("receive ack which not match l1 item in have send list,check if match l1 item is sending item ");
      
      if(NULL != sending_item && sending_item->tag == be16_2_cpu(header->sequence_id)) {
        LOGI("receive ack for sending item ");
        if(is_error_flag_set(header)) {
          LOGI("error ack packet");
          sending_item->status |= L1_ITEM_HAVE_ACK_ERROR;
        } else {
          LOGI("success ack packet");
          sending_item->status |= L1_ITEM_HAVE_ACK_OK;
        }
        L1_have_receive_length -= block_L1_size + sizeof(health_ble_L1_header_t);
        block_L1_size = 0;
        unlock_l0_implement();
        return;
      }
    }

    
    if(!find_match_l1_item) {
      LOGE("cannot find match l1 item, maybe ACK time have been exhausted ");
      assert(0);
      L1_have_receive_length -= block_L1_size + sizeof(health_ble_L1_header_t);
      block_L1_size = 0;
      unlock_l0_implement();
      return;
    }
    
    if(is_error_flag_set(header)) {
      LOGI("error ack packet");
      if(NULL != send_callback_l1) {
        send_callback_l1(pool_item->tag, -1);
      }
    } else {
      LOGI("success ack packet");
      if(NULL != send_callback_l1) {
        send_callback_l1(pool_item->tag, 0);
      }
    }
    list_del(&(pool_item->list));
    free_L1_pool_item(pool_item);
    
    send_l1_item_from_cb(); //in this version,we only send next L2 packet after receive previous L2 packet ack or timeout
    
  } else {
    LOGI("receive L1 data  packet ");
    crc = crc16(0,(const unsigned char*)(L1_recv_buffer + sizeof(health_ble_L1_header_t)), block_L1_size);
    if(be16_2_cpu(header->crc16) != crc || header->magic != L1_MAGIC) {
      LOGE("L1 crc or magic error, need :0x%x real:0x%x  magic:%x",be16_2_cpu(header->crc16),crc,header->magic);
      send_ack_packet(1,be16_2_cpu(header->sequence_id));
      L1_have_receive_length -= block_L1_size + sizeof(health_ble_L1_header_t);
      assert(0 == L1_have_receive_length);
      block_L1_size = 0;
      
      error_happen_count ++;
      
      if(error_happen_count > MAX_ERROR_TORRENT && NULL != need_reset_l1) {
        LOGI("need reset the stack");
        need_reset_l1();
      }
      
      unlock_l0_implement();
      return;
    } else {
      send_ack_packet(0,be16_2_cpu(header->sequence_id));
      DUMP_L1_DATA(block_L1_size + sizeof(health_ble_L1_header_t), L1_recv_buffer);
      if(NULL != recv_callback_l1) {
        recv_callback_l1(block_L1_size,L1_recv_buffer + sizeof(health_ble_L1_header_t));
      }
    }
  }
  
  L1_have_receive_length -= block_L1_size + sizeof(health_ble_L1_header_t);
  block_L1_size = 0;
  
  
  unlock_l0_implement();
  
}

void L0_sent_data_cb(uint16_t seq_id, int status_code)
{
  L0_mtu_item_t *l0_item = NULL;
  struct list_head *p;
  unsigned char all_l0_have_send = 1;
  unsigned char find_match_l0_item = 0;
  health_ble_L1_header_t *header;
  int ret;
  
  LOGI("L0_sent_data_cb");
  
  if(0 == have_been_init) {
    LOGE("L1 have not been init");
    return;
  }
  
  lock_l0_implement();
  
  if(0 == have_been_init) {
    LOGE("L1 have not been init");
    return;
  }
  
  LOGI("l0  tag: 0x%x status:%d",seq_id,status_code);
  
  if(NULL == sending_item) {
    LOGE("no sending_item exist");
    unlock_l0_implement();
    assert(0);
    return;
  }
  
  list_for_each(p,&(sending_item->L0_mtu_list)) {
    l0_item = (L0_mtu_item_t*)p;
    if(seq_id == l0_item->tag) {
      find_match_l0_item = 1;
      break;
    }
  }
  
  
  if(!(find_match_l0_item) || NULL == l0_item) {
    LOGE("can not find match l0_item");
    assert(0);
    unlock_l0_implement();
    return;
  }
  
  if(0 == status_code) {
    l0_item->status |= L0_ITEM_HAVE_SENDED;
    
    list_for_each(p, &(sending_item->L0_mtu_list)) {
      l0_item = (L0_mtu_item_t*)p;
      if(!(l0_item->status & L0_ITEM_HAVE_SENDED)) {
        all_l0_have_send = 0;
        break;
      }
    }
   
    if(all_l0_have_send) {
      LOGI("all L0 item have been send");
      sending_item->status |= L1_ITEM_HAVE_SENDED;
      sending_item->send_time_count = l1_time_count;
      list_del(&(sending_item->list));
      pool_item_size --;
      header = (health_ble_L1_header_t*)(sending_item->data);
      if(is_ack_packet(header)) {
        LOGI("is a ack pocket");
        free_L1_pool_item(sending_item);
        sending_item = NULL;
        ret = -1;
        send_l1_item_from_cb(); //in this version,we only send next L2 packet after receive previous L2 packet ack or timeout
      } else {
        if((sending_item->status&L1_ITEM_HAVE_ACK_OK) || (sending_item->status&L1_ITEM_HAVE_ACK_ERROR)) {
          LOGI("L1 item onSend callback after ACK l1 tag:0x%x status:0x%x",sending_item->tag,sending_item->status);
          if(sending_item->status&L1_ITEM_HAVE_ACK_ERROR) {
            if(NULL != send_callback_l1) {
              send_callback_l1(sending_item->tag, -1);
            }
          } else {
            if(NULL != send_callback_l1) {
              send_callback_l1(sending_item->tag, 0);
            }
          }
          free_L1_pool_item(sending_item);
          sending_item = NULL;
          send_l1_item_from_cb(); //in this version,we only send next L2 packet after receive previous L2 packet ack or timeout

        } else {
 
          LOGI("is a data packet,add to have_send_list");
          list_add_tail(&(sending_item->list), &L1_have_send_list);
          sending_item = NULL;
          
          if(list_size(&L1_have_send_list) < WAIT_ACK_L1_ITEM_MAX) {
              send_l1_item_from_cb();
          }
        }
      }
    } else {
      LOGI("send next L0 item l0 tag:0x%x",l0_item->tag);
      if(!(l0_item->status & L0_ITEM_IN_SENDING)) {
        send_l0_item_from_cb(l0_item);
      } else {
        LOGI("next L0 item l0 tag:0x%x have been sending, should not sending it again",l0_item->tag);
      }
    }
  } else {
    LOGI("resend the L0 item l0 tag:0x%x",l0_item->tag);
    send_l0_item_from_cb(l0_item);
    
    error_happen_count ++;
    
    if(error_happen_count > MAX_ERROR_TORRENT && NULL != need_reset_l1) {
      LOGI("need reset the stack");
      need_reset_l1();
    }
  }
  unlock_l0_implement();
}

int send_L1(uint16_t length,char *data ){
  health_ble_L1_header_t header;
  LOGI("send_L1" );
  
  if(0 == have_been_init) {
    LOGE("L1 have not been init");
    return -1;
  }
  
  
  if(0 == length || length > L1_MTU || NULL == data) {
      LOGE("the input param is not legal" );
      return -1;
  }
  
  tag_L1 ++;
  
  memset(&header,0,sizeof(header));
  header.magic = L1_MAGIC;
  L1_set_version(&header, L1_VERSION);
  header.payload_length = cpu_2_be16(length);
  header.crc16 = cpu_2_be16(crc16(0, (unsigned char*)data, length));
  header.sequence_id = tag_L1;
  header.sequence_id = cpu_2_be16(header.sequence_id);
  
  return send_data_with_header(&header,length,data,0,0);
  
}

void handle_have_send_list_ack_time_expire() {
  struct list_head *p,*n;
  list_for_each_safe(p,n,&L1_have_send_list) {
    L1_pool_item_t *pool_item;
    pool_item = (L1_pool_item_t *)p;
    
    if((pool_item->status&L1_ITEM_HAVE_SENDED)&&
       ((pool_item->send_time_count > l1_time_count && l1_time_count > L1_ACK_TIME_EXPIRE)
       ||(pool_item->send_time_count < l1_time_count && (l1_time_count - pool_item->send_time_count) >= L1_ACK_TIME_EXPIRE))
       ){
      LOGI("ack time expire l1 tag:%d",pool_item->tag);
      if(NULL != send_callback_l1) {
        send_callback_l1(pool_item->tag, -1);
        send_l1_item_from_cb(); //in this version,we only send next L2 packet after receive previous L2 packet ack or timeout
        list_del(&(pool_item->list));
        free_L1_pool_item(pool_item);
      }
    }
  }
  
  if(NULL == sending_item && list_size(&L1_have_send_list) < WAIT_ACK_L1_ITEM_MAX) {
    send_l1_item_from_cb();
  }
}

void on_timer_fire_l1() {
//  LOGI("on_timer_fire_l1" );
  
  if(0 == have_been_init) {
    LOGE("L1 have not been init");
    return;
  }
  
  l1_time_count ++;

  handle_have_send_list_ack_time_expire();
}



int init_health_ble_L1(recv_cb_L1 recv_cb, send_cb_L1 send_cb,create_timer_L1 create_timer, stop_timer_L1 stop_timer,lock_stack_L1 lock_l1,unlock_stack_L1 unlock_l1,need_reset_L1 need_reset) {
  int ret;
  
  LOGI("init_health_ble_L1" );
  if (NULL == recv_cb || NULL == send_cb || NULL == create_timer) {
    return -1;
  }
  
  recv_callback_l1 = recv_cb;
  send_callback_l1 = send_cb;
  create_timer_l1 = create_timer;
  stop_timer_l1 = stop_timer;
  
  lock_stack_l1 = lock_l1;
  unlock_stack_l1 = unlock_l1;
  
  need_reset_l1 = need_reset;
  
  create_timer_l1(on_timer_fire_l1,L1_TIME_FIRE_INTERVAL);
  
  INIT_LIST_HEAD(&L1_pool_list);
  
  INIT_LIST_HEAD(&L1_ack_pool_list);
  
  INIT_LIST_HEAD(&L1_have_send_list);
  
  tag_L1 = 0;
  block_L1_size = 0;
  pool_item_size = 0;
  sending_item = NULL;
  
  L1_have_receive_length = 0;
  
  error_happen_count = 0;
  
  ret = init_health_ble_L0(L0_receive_data_cb, L0_sent_data_cb);
  if(0 == ret) {
    have_been_init = 1;
    
  } else {
    LOGE("init_health_ble_L0 error" );
    stop_timer_l1();
    sending_item = NULL;
    
    recv_callback_l1 = NULL;
    send_callback_l1 = NULL;
    create_timer_l1 = NULL;
    stop_timer_l1 = NULL;
    
    lock_stack_l1 = NULL;
    unlock_stack_l1 = NULL;
    
    need_reset_l1 = NULL;
  }
  
  return ret;
}

int finalize_health_ble_L1() {
  struct list_head *p,*n;
  
  LOGI("finalize_health_ble_L1");
  
  if(!have_been_init) {
    return -1;
  }
  
  stop_timer_l1();
  
  list_for_each_safe(p, n,&L1_have_send_list) {
    L1_pool_item_t *pool_item = (L1_pool_item_t *)p;
    if(NULL != send_callback_l1) {
      send_callback_l1(pool_item->tag, -1);
    }
    free_L1_pool_item(pool_item);
  }
  
  list_for_each_safe(p, n,&L1_pool_list) {
    L1_pool_item_t *pool_item = (L1_pool_item_t *)p;
    if(NULL != send_callback_l1) {
      send_callback_l1(pool_item->tag, -1);
    }
    free_L1_pool_item(pool_item);
  }
  
  list_for_each_safe(p, n,&L1_ack_pool_list) {
    L1_pool_item_t *pool_item = (L1_pool_item_t *)p;
    free_L1_pool_item(pool_item);
  }
  
  tag_L1 = 0;
  block_L1_size = 0;
  pool_item_size = 0;
  sending_item = NULL;
  
  recv_callback_l1 = NULL;
  send_callback_l1 = NULL;
  create_timer_l1 = NULL;
  stop_timer_l1 = NULL;
  
  lock_stack_l1 = NULL;
  unlock_stack_l1 = NULL;
  
  need_reset_l1 = NULL;
  
  L1_have_receive_length = 0;
  
  have_been_init = 0;
  
  error_happen_count = 0;
  
  return 0;
  
}
