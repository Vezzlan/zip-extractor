package com.zip.model;

import java.util.zip.ZipEntry;

public record FilePair(ZipEntry json, ZipEntry python) {}
