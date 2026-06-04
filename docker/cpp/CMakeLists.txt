cmake_minimum_required(VERSION 3.14)
project(sc-cpp)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

if(EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/main.cpp")
  add_executable(app main.cpp)
endif()

if(EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/sc_test.cc")
  include(FetchContent)
  FetchContent_Declare(
    googletest
    URL https://github.com/google/googletest/archive/03597a01ee50ed33e9dfd640b249b4be3799d395.zip
  )
  set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
  FetchContent_MakeAvailable(googletest)

  enable_testing()

  add_executable(sc_test sc_test.cc solution.hpp)
  target_link_libraries(sc_test GTest::gtest_main)

  include(GoogleTest)
  gtest_discover_tests(sc_test)
endif()
