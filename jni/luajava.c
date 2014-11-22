#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
#include "luasocket.h"  
#include "mime.h" 

#define  LOG_TAG    "libluajava"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

static lua_State * L = NULL;

#define BUFFER_MAX_LEN 4096

struct block {
    char buffer[BUFFER_MAX_LEN];
    char * ptr;
};

static struct block block_output;

static const char * lua_path = "\
local path=...\
package.path = package.path..path..'/?.lua;'\
package.cpath = package.cpath..path..'/?.so;'\
";

static void lua_addpath(lua_State *L, const char * path) {
    int err = luaL_loadstring(L, lua_path);
    assert(err == LUA_OK);
    lua_pushstring(L, path);
    err = lua_pcall(L, 1, 0, 0);
    if (err) {
        fprintf(stderr,"%s\n",lua_tostring(L,-1));
        lua_close(L);
        exit(1);
    }
}

static void foutput(const char * val) {
    int n = strlen(val);
    int rn = BUFFER_MAX_LEN-(block_output.ptr-block_output.buffer);
    if (rn>=n) {
        strncpy(block_output.ptr,val,n);
        block_output.ptr += n;
    }
}

static int lua_print (lua_State *L) {
  int n = lua_gettop(L);  /* number of arguments */
  int i;
  lua_getglobal(L, "tostring");
  for (i=1; i<=n; i++) {
    const char *s;
    lua_pushvalue(L, -1);  /* function to be called */
    lua_pushvalue(L, i);   /* value to print */
    lua_call(L, 1, 1);
    s = lua_tostring(L, -1);  /* get result */
    if (s == NULL)
      return luaL_error(L, LUA_QL("tostring") " must return a string to "
                           LUA_QL("print"));
    if (i>1) foutput("\t");
    foutput(s);
    lua_pop(L, 1);  /* pop result */
  }
  foutput("\n");
  return 0;
}

static void cleanoutput() {
    memset(block_output.buffer,0,BUFFER_MAX_LEN);
    block_output.ptr = block_output.buffer;
}

void
Java_com_hanxi_luarun_MainActivity_luainit( JNIEnv * env, jobject jobj, jstring wpath)
{
    if (L) {
        lua_close(L);
    }

    L = luaL_newstate();
    luaL_checkversion(L);
    luaL_openlibs(L);   // link lua lib
    luaL_requiref(L, "mime.core", luaopen_mime_core, 0);
    luaL_requiref(L, "socket.core", luaopen_socket_core, 0);
    lua_settop(L,0);

    const char *path = (*env)->GetStringUTFChars(env, wpath, NULL);
    lua_addpath(L,path);
    lua_register(L,"print",lua_print);
    cleanoutput();
}

void
Java_com_hanxi_luarun_MainActivity_luaaddpath( JNIEnv * env , jobject jobj, jstring path)
{
    // convert Java string to UTF-8
    const char *utf8 = (*env)->GetStringUTFChars(env, path, NULL);
    assert(NULL != utf8);
    lua_addpath(L,utf8);
}

jstring Java_com_hanxi_luarun_MainActivity_luadostring( JNIEnv * env , jobject jobj, jstring luastr)
{
    const char * utfStr = ( *env )->GetStringUTFChars( env , luastr , NULL );
    LOGI("lua:%s",utfStr);
    luaL_loadstring(L, "msg=...;print(msg)");
    luaL_loadstring(L, utfStr);
    lua_pcall(L,0,0,-2);
    lua_settop(L,0);
    foutput("\0");
    return (*env)->NewStringUTF(env, block_output.buffer);
}

void Java_com_hanxi_luarun_MainActivity_luacleanoutput( JNIEnv * env , jobject jobj)
{
    cleanoutput();
}

jstring
Java_com_hanxi_luarun_MainActivity_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
#if defined(__arm__)
  #if defined(__ARM_ARCH_7A__)
    #if defined(__ARM_NEON__)
      #define ABI "armeabi-v7a/NEON"
    #else
      #define ABI "armeabi-v7a"
    #endif
  #else
   #define ABI "armeabi"
  #endif
#elif defined(__i386__)
   #define ABI "x86"
#elif defined(__mips__)
   #define ABI "mips"
#else
   #define ABI "unknown"
#endif

    return (*env)->NewStringUTF(env, "Hello from JNI !  Compiled with ABI " ABI ".");
}

