package com.olam.warehouse.vegax.gateentrynigeria.utils

import androidx.core.content.FileProvider

public class CustomFileProvider : FileProvider()
{
    //we extend fileprovider to stop collision with chatbot library file provider
    //this class is empty and used in manifest
}
