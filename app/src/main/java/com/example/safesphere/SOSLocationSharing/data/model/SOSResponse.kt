package com.example.safesphere.SOSLocationSharing.data.model

import com.example.safesphere.SOSLocationSharing.data.model.Guardian

data class SOSResponse(
    val success: Boolean,
    val guardiansList:List<Guardian>
)