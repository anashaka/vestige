#include "ProxySchemasGSettingsAccess.h"
#include <gio/gio.h>
#include <string.h>
#include <stdlib.h>

struct GSettingsList {
	const char * schemaName;
	GSettings* client;
	struct GSettingsList * next;
};

struct GSettingsList * proxySchemas;

int startsWith(const char *pre, const char *str) {
    size_t lenpre = strlen(pre),
	lenstr = strlen(str);
    return lenstr < lenpre ? 0 : strncmp(pre, str, lenpre) == 0;
}

__attribute__((constructor)) void init() {
	g_type_init();
	proxySchemas = 0;
    const gchar* const* schemas = g_settings_list_schemas();
    while (*schemas) {
		if (startsWith("org.gnome.system.proxy", (const char *)(*schemas))) {
			struct GSettingsList * nclients = (struct GSettingsList *) malloc(sizeof(struct GSettingsList));
			nclients->next = proxySchemas;
			nclients->schemaName = *schemas;
			nclients->client = g_settings_new(*schemas);
			proxySchemas = nclients;
		}
		schemas++;
    }	
}

__attribute__((destructor)) void destroy() {
	struct GSettingsList * proxySchemasIt = proxySchemas;
	while (proxySchemasIt != 0) {
		struct GSettingsList * next = proxySchemasIt->next;
		free(proxySchemasIt);
		proxySchemasIt = next;
	}
	proxySchemas = 0;
}

void convertKey(JNIEnv *env, jmethodID put, jobject hashMap, gchar* gkey, GSettings* schema, GVariant * gvalue) {
	jstring key = (*env)->NewStringUTF(env, gkey);
	jobject value = 0;
		
	const GVariantType * t = g_variant_get_type(gvalue);
	if (g_variant_type_equal(t, G_VARIANT_TYPE_STRING)) {
		const gchar * gstring = g_variant_get_string(gvalue, 0);
		value = (*env)->NewStringUTF(env, gstring);
	} else if (g_variant_type_equal(t, G_VARIANT_TYPE_BOOLEAN)) {
		jclass bClass = (*env)->FindClass(env, "java/lang/Boolean");	
		jmethodID valueOf = (*env)->GetStaticMethodID(env, bClass, "valueOf", "(Z)Ljava/lang/Boolean;");
		value = (*env)->CallStaticObjectMethod(env, bClass, valueOf, g_variant_get_boolean(gvalue));
		(*env)->DeleteLocalRef(env, bClass);
	} else if (g_variant_type_equal(t, G_VARIANT_TYPE_INT32)) {
		jclass bClass = (*env)->FindClass(env, "java/lang/Integer");	
		jmethodID valueOf = (*env)->GetStaticMethodID(env, bClass, "valueOf", "(I)Ljava/lang/Integer;");
		value = (*env)->CallStaticObjectMethod(env, bClass, valueOf, g_variant_get_int32(gvalue));
		(*env)->DeleteLocalRef(env, bClass);
	} else if (g_variant_type_equal(t, G_VARIANT_TYPE_STRING_ARRAY)) {
		int size = g_variant_n_children(gvalue);
		jclass bClass = (*env)->FindClass(env, "java/util/ArrayList");	
		jmethodID init = (*env)->GetMethodID(env, bClass, "<init>", "(I)V");
		jmethodID add = (*env)->GetMethodID(env, bClass, "add", "(Ljava/lang/Object;)Z");
		value = (*env)->NewObject(env, bClass, init, size);
		int i;
		for (i = 0; i < size; i++) {
			GVariant * gsvalue = g_variant_get_child_value(gvalue, i);
			const gchar * gstring = g_variant_get_string(gsvalue, 0);
			jobject svalue = (*env)->NewStringUTF(env, gstring);
			(*env)->CallObjectMethod(env, value, add, svalue);
			(*env)->DeleteLocalRef(env, svalue);
		}
		(*env)->DeleteLocalRef(env, bClass);
	}

	if (value != 0) {
		(*env)->CallObjectMethod(env, hashMap, put, key, value);
		(*env)->DeleteLocalRef(env, value);
	}
	(*env)->DeleteLocalRef(env, key);
}

void convertSchema(JNIEnv *env, jclass mapClass, jmethodID init, jmethodID put, jobject hashMap, const char * schemaName, GSettings* schema) {
	jobject subHashMap = (*env)->NewObject(env, mapClass, init);
	jstring jschemaName = (*env)->NewStringUTF(env, schemaName);
	gchar** keys = g_settings_list_keys(schema);
    while (*keys) {
		convertKey(env, put, subHashMap, *keys, schema, g_settings_get_value(schema, *keys));
		keys++;
    }
	(*env)->CallObjectMethod(env, hashMap, put, jschemaName, subHashMap);
	(*env)->DeleteLocalRef(env, subHashMap);
	(*env)->DeleteLocalRef(env, jschemaName);
}

JNIEXPORT jobject JNICALL Java_com_btr_proxy_search_desktop_gnome_ProxySchemasGSettingsAccess_getValueByKeyBySchema(JNIEnv *env, jclass c) {
	jclass mapClass = (*env)->FindClass(env, "java/util/HashMap");	
		
	jmethodID init = (*env)->GetMethodID(env, mapClass, "<init>", "()V");
	jobject hashMap = (*env)->NewObject(env, mapClass, init);
	
	jmethodID put = (*env)->GetMethodID(env, mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	struct GSettingsList * proxySchemasIt = proxySchemas;
	while (proxySchemasIt != 0) {
		convertSchema(env, mapClass, init, put, hashMap, proxySchemasIt->schemaName, proxySchemasIt->client);
		proxySchemasIt = proxySchemasIt->next;
	}
	
	(*env)->DeleteLocalRef(env, mapClass);
	return hashMap;	
}

