package com.olam.warehouse.presentation.converters

import androidx.room.TypeConverter
import com.olam.warehouse.presentation.enums.Status

class EnumTypeConverter {

    @TypeConverter
    fun restoreEnum(enumName: String): Status = Status.valueOf(enumName)

    @TypeConverter
    fun saveEnumToString(enumType: Status) = enumType.name
}
