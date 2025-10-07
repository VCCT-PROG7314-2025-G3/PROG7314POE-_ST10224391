package com.example.swoptrader.data.local

import androidx.room.TypeConverter
import com.example.swoptrader.data.model.MeetupStatus
import com.example.swoptrader.data.model.OfferStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
    
    @TypeConverter
    fun fromOfferStatus(status: OfferStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toOfferStatus(status: String): OfferStatus {
        return OfferStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromMeetupStatus(status: MeetupStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toMeetupStatus(status: String): MeetupStatus {
        return MeetupStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromStringMap(value: Map<String, Int>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String): Map<String, Int> {
        return Gson().fromJson(value, object : TypeToken<Map<String, Int>>() {}.type)
    }
    
    @TypeConverter
    fun fromStringListMap(value: Map<String, List<String>>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringListMap(value: String): Map<String, List<String>> {
        return Gson().fromJson(value, object : TypeToken<Map<String, List<String>>>() {}.type)
    }
    
    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return Gson().fromJson(value, object : TypeToken<List<Long>>() {}.type)
    }
}