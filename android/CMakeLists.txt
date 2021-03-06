if(MBP_TOP_LEVEL_BUILD)
    include(ExternalProject)

    set(ANDROID_TOOLCHAIN_VERSION 4.9)
    set(ANDROID_ABIS armeabi-v7a arm64-v8a x86 x86_64)

    function(get_toolchain_name toolchain_var abi)
        if(${abi} STREQUAL armeabi-v7a)
            set(${toolchain_var}
                arm-linux-androideabi-${ANDROID_TOOLCHAIN_VERSION}
                PARENT_SCOPE)
        elseif(${abi} STREQUAL arm64-v8a)
            set(${toolchain_var}
                aarch64-linux-android-${ANDROID_TOOLCHAIN_VERSION}
                PARENT_SCOPE)
        elseif(${abi} STREQUAL x86)
            set(${toolchain_var}
                x86-${ANDROID_TOOLCHAIN_VERSION}
                PARENT_SCOPE)
        elseif(${abi} STREQUAL x86_64)
            set(${toolchain_var}
                x86_64-${ANDROID_TOOLCHAIN_VERSION}
                PARENT_SCOPE)
        else()
            message(FATAL_ERROR "Invalid ABI: ${abi}")
        endif()
    endfunction()

    # Always make release builds for the system components unless explicitly
    # told not to since a debug build of mbtool is too large to fit in some
    # devices' ramdisks.
    if(${MBP_SYSTEM_BUILD_TYPE} STREQUAL debug)
        set(MBP_SYSTEM_CMAKE_BUILD_TYPE Debug)
    else()
        set(MBP_SYSTEM_CMAKE_BUILD_TYPE Release)
    endif()

    # List of ExternalProject directories to run "make clean" in
    set(RECURSIVE_CLEAN_DIRS)

    foreach(abi ${ANDROID_ABIS})
        get_toolchain_name(toolchain_name ${abi})

        set(INTERNAL_COMMON_OPTIONS
            -DCMAKE_TOOLCHAIN_FILE=${CMAKE_SOURCE_DIR}/external/android-cmake/android.toolchain.cmake
            -DCMAKE_INSTALL_PREFIX=<INSTALL_DIR>
            -DANDROID_ABI=${abi}
            -DANDROID_TOOLCHAIN_NAME=${toolchain_name}
            -DANDROID_STL=gnustl_static
            -DMBP_BUILD_TYPE=${MBP_BUILD_TYPE}
            -DMBP_ENABLE_TESTS=OFF
            -DMBP_INTERNAL_PREBUILTS_DIR=${MBP_PREBUILTS_DIR}
            -DMBP_SIGN_JAVA_KEYSTORE_PATH=${MBP_SIGN_JAVA_KEYSTORE_PATH}
            -DMBP_SIGN_JAVA_KEYSTORE_PASSWORD=${MBP_SIGN_JAVA_KEYSTORE_PASSWORD}
            -DMBP_SIGN_JAVA_KEY_ALIAS=${MBP_SIGN_JAVA_KEY_ALIAS}
            -DMBP_SIGN_JAVA_KEY_PASSWORD=${MBP_SIGN_JAVA_KEY_PASSWORD}
            -DJAVA_KEYTOOL=${JAVA_KEYTOOL}
        )

        if(MBP_ANDROID_ENABLE_CCACHE)
            list(APPEND INTERNAL_COMMON_OPTIONS "-DNDK_CCACHE=${MBP_CCACHE_PATH}")
        endif()

        set(INTERNAL_ANDROID_APP_OPTIONS
            ${INTERNAL_COMMON_OPTIONS}
            -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
            -DANDROID_APP_PIE=ON
            -DANDROID_NATIVE_API_LEVEL=17
            -DMBP_BUILD_TARGET=android-app
        )

        set(INTERNAL_ANDROID_SYSTEM_OPTIONS
            ${INTERNAL_COMMON_OPTIONS}
            -DCMAKE_BUILD_TYPE=${MBP_SYSTEM_CMAKE_BUILD_TYPE}
            -DANDROID_APP_PIE=OFF
            -DANDROID_NATIVE_API_LEVEL=21
            -DMBP_BUILD_TARGET=android-system
        )

        message(STATUS "Recursive CMake build for Android system components (${abi}):")
        foreach(_option ${INTERNAL_ANDROID_SYSTEM_OPTIONS})
            string(REGEX REPLACE "([^=]*PASSWORD[^=]*=).*" "\\1<REDACTED>"
                   _option "${_option}")
            message(STATUS "  ${_option}")
        endforeach()

        # Always build system components
        ExternalProject_Add(
            android-system_${abi}
            SOURCE_DIR ${CMAKE_SOURCE_DIR}
            INSTALL_DIR ${CMAKE_CURRENT_BINARY_DIR}/result
            CMAKE_ARGS ${INTERNAL_ANDROID_SYSTEM_OPTIONS}
            BUILD_ALWAYS 1
            INSTALL_COMMAND ${CMAKE_COMMAND} --build . --target install/strip
            VERBATIM
        )

        ExternalProject_Get_Property(android-system_${abi} BINARY_DIR)
        list(APPEND RECURSIVE_CLEAN_DIRS ${BINARY_DIR})

        install(
            FILES ${CMAKE_CURRENT_BINARY_DIR}/result/bin/${abi}/mbtool
                  ${CMAKE_CURRENT_BINARY_DIR}/result/bin/${abi}/mbtool_recovery
                  ${CMAKE_CURRENT_BINARY_DIR}/result/bin/${abi}/odinupdater
                  ${CMAKE_CURRENT_BINARY_DIR}/result/bin/${abi}/fuse-sparse
            DESTINATION ${DATA_INSTALL_DIR}/binaries/android/${abi}/
            COMPONENT Libraries
        )

        # Build app components only if we're targeting Android
        if(${MBP_BUILD_TARGET} STREQUAL android)
            message(STATUS "Recursive CMake build for Android app components (${abi}):")
            foreach(_option ${INTERNAL_ANDROID_APP_OPTIONS})
                string(REGEX REPLACE "([^=]*PASSWORD[^=]*=).*" "\\1<REDACTED>"
                       _option "${_option}")
                message(STATUS "  ${_option}")
            endforeach()

            ExternalProject_Add(
                android-app_${abi}
                SOURCE_DIR ${CMAKE_SOURCE_DIR}
                INSTALL_DIR ${CMAKE_CURRENT_BINARY_DIR}/result
                CMAKE_ARGS ${INTERNAL_ANDROID_APP_OPTIONS}
                BUILD_ALWAYS 1
                INSTALL_COMMAND ${CMAKE_COMMAND} --build . --target install/strip
                VERBATIM
            )

            ExternalProject_Get_Property(android-app_${abi} BINARY_DIR)
            list(APPEND RECURSIVE_CLEAN_DIRS ${BINARY_DIR})
        endif()
    endforeach()

    # Create clean target
    set(CLEAN_CONTENTS)
    foreach(_dir ${RECURSIVE_CLEAN_DIRS})
        string(CONCAT CLEAN_CONTENTS
               ${CLEAN_CONTENTS}
               "message(STATUS \"Cleaning ${_dir}\")\n"
               "execute_process(COMMAND \${CMAKE_COMMAND} --build \"${_dir}\" --target clean)\n")
    endforeach()

    file(WRITE
         ${CMAKE_CURRENT_BINARY_DIR}/clean-android.cmake
         "${CLEAN_CONTENTS}")

    add_custom_target(
        clean-android
        ${CMAKE_COMMAND} -P ${CMAKE_CURRENT_BINARY_DIR}/clean-android.cmake
        VERBATIM
    )
endif()
