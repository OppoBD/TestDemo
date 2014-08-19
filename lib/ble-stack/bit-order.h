//
//  data_order.h
//  baiduhealth
//
//  Created by zhangdongsheng on 13-10-24.
//  Copyright (c) 2013年 Baidu. All rights reserved.
//

#ifndef baiduhealth_bit_order_h
#define baiduhealth_bit_order_h
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

#define LE_END 1
    
#if LE_END
    
static inline uint32_t cpu_2_be32(x) {
    uint32_t ret;
    ret = (x&0xff000000) >> 24;
    ret += (x&0xff0000) >> 8;
    ret += (x&0xff00) << 8;
    ret += (x&0xff) << 24;
    return ret;
}

static inline uint16_t cpu_2_be16(x) {
    uint16_t ret;
    ret = (x&0xff00) >> 8;
    ret += (x&0xff) << 8;
    return ret;
}
    

static inline uint32_t be32_2_cpu(x) {
    uint32_t ret;
    ret = (x&0xff000000) >> 24;
    ret += (x&0xff0000) >> 8;
    ret += (x&0xff00) << 8;
    ret += (x&0xff) << 24;
    return ret;
}
    
static inline uint16_t be16_2_cpu(x) {
    uint16_t ret;
    ret = (x&0xff00) >> 8;
    ret += (x&0xff) << 8;
    return ret;
}

#else
static inline uint32_t cpu_2_be32(x) {
    return x;
}
    
static inline uint16_t cpu_2_be16(x) {
    return x;
}
    
inline uint32_t be32_2_cpu(x) {
    return x;
}

inline uint16_t be16_2_cpu(x) {
    return x;
}
    
#endif


#ifdef __cplusplus
}
#endif

#endif
