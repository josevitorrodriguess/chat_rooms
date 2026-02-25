package com.josevitor.chatrooms.config;

import java.util.Map;

public record ApiError(String message, Map<String, String> fieldErrors) {
}
