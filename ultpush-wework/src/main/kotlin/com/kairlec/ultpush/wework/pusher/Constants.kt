package com.kairlec.ultpush.wework.pusher

//import com.fasterxml.jackson.annotation.JsonSetter
//import com.fasterxml.jackson.annotation.Nulls
//import com.fasterxml.jackson.databind.DeserializationFeature
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.databind.PropertyNamingStrategy
//import com.fasterxml.jackson.databind.cfg.MapperConfig
//import com.fasterxml.jackson.databind.introspect.AnnotatedField
//import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
//import com.kairlec.ultpush.wework.utils.CustomSerializerProvider
//
//class AppendPrefixStrategy : PropertyNamingStrategy() {
//    override fun nameForField(config: MapperConfig<*>, field: AnnotatedField, defaultName: String): String {
//        return translate(defaultName)
//    }
//
//    override fun nameForGetterMethod(config: MapperConfig<*>, method: AnnotatedMethod, defaultName: String): String {
//        return translate(defaultName)
//    }
//
//    private fun translate(input: String): String {
//        return input.replace("to([A-Z])".toRegex()) {
//            "to${it.groupValues[1].toLowerCase()}"
//        }.replace("[A-Z]".toRegex()) {
//            "_${it.groupValues[0].toLowerCase()}"
//        }
//    }
//}
//
//val objectMapper = run {
//    val mapper = ObjectMapper()
//    mapper.configOverride(String::class.java).setterInfo = JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY)
//    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//    mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
//    mapper.setSerializerProvider(CustomSerializerProvider())
//    mapper.propertyNamingStrategy = AppendPrefixStrategy()
//    mapper
//}
