# appifyhub.MessagingApi

All URIs are relative to *https://api.appifyhub.com*

Method | HTTP request | Description
------------- | ------------- | -------------
[**add_push_device**](MessagingApi.md#add_push_device) | **POST** /v1/universal/users/{universalId}/push-devices | Add a push device
[**fetch_all_push_devices_for_user**](MessagingApi.md#fetch_all_push_devices_for_user) | **GET** /v1/universal/users/{universalId}/push-devices | Get all push devices
[**fetch_push_device**](MessagingApi.md#fetch_push_device) | **GET** /v1/universal/users/{universalId}/push-devices/{deviceId} | Get a push device
[**remove_all_push_devices_for_user**](MessagingApi.md#remove_all_push_devices_for_user) | **DELETE** /v1/universal/users/{universalId}/push-devices | Remove all push devices
[**remove_push_device**](MessagingApi.md#remove_push_device) | **DELETE** /v1/universal/users/{universalId}/push-devices/{deviceId} | Remove a push device
[**send_message**](MessagingApi.md#send_message) | **POST** /v1/projects/{projectId}/users/{universalId}/message | Send a message to a user


# **add_push_device**
> PushDeviceResponse add_push_device(universal_id, push_device_request)

Add a push device

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.push_device_request import PushDeviceRequest
from appifyhub.models.push_device_response import PushDeviceResponse
from appifyhub.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api.appifyhub.com
# See configuration.py for a list of all supported configuration parameters.
configuration = appifyhub.Configuration(
    host = "https://api.appifyhub.com"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure Bearer authorization (JWT): BearerAuth
configuration = appifyhub.Configuration(
    access_token = os.environ["BEARER_TOKEN"]
)

# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.MessagingApi(api_client)
    universal_id = 'universal_id_example' # str | 
    push_device_request = appifyhub.PushDeviceRequest() # PushDeviceRequest | 

    try:
        # Add a push device
        api_response = api_instance.add_push_device(universal_id, push_device_request)
        print("The response of MessagingApi->add_push_device:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->add_push_device: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 
 **push_device_request** | [**PushDeviceRequest**](PushDeviceRequest.md)|  | 

### Return type

[**PushDeviceResponse**](PushDeviceResponse.md)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **fetch_all_push_devices_for_user**
> PushDevicesResponse fetch_all_push_devices_for_user(universal_id)

Get all push devices

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.push_devices_response import PushDevicesResponse
from appifyhub.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api.appifyhub.com
# See configuration.py for a list of all supported configuration parameters.
configuration = appifyhub.Configuration(
    host = "https://api.appifyhub.com"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure Bearer authorization (JWT): BearerAuth
configuration = appifyhub.Configuration(
    access_token = os.environ["BEARER_TOKEN"]
)

# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.MessagingApi(api_client)
    universal_id = 'universal_id_example' # str | 

    try:
        # Get all push devices
        api_response = api_instance.fetch_all_push_devices_for_user(universal_id)
        print("The response of MessagingApi->fetch_all_push_devices_for_user:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->fetch_all_push_devices_for_user: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 

### Return type

[**PushDevicesResponse**](PushDevicesResponse.md)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **fetch_push_device**
> PushDeviceResponse fetch_push_device(universal_id, device_id)

Get a push device

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.push_device_response import PushDeviceResponse
from appifyhub.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api.appifyhub.com
# See configuration.py for a list of all supported configuration parameters.
configuration = appifyhub.Configuration(
    host = "https://api.appifyhub.com"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure Bearer authorization (JWT): BearerAuth
configuration = appifyhub.Configuration(
    access_token = os.environ["BEARER_TOKEN"]
)

# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.MessagingApi(api_client)
    universal_id = 'universal_id_example' # str | 
    device_id = 'device_id_example' # str | 

    try:
        # Get a push device
        api_response = api_instance.fetch_push_device(universal_id, device_id)
        print("The response of MessagingApi->fetch_push_device:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->fetch_push_device: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 
 **device_id** | **str**|  | 

### Return type

[**PushDeviceResponse**](PushDeviceResponse.md)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **remove_all_push_devices_for_user**
> SimpleResponse remove_all_push_devices_for_user(universal_id)

Remove all push devices

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.simple_response import SimpleResponse
from appifyhub.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api.appifyhub.com
# See configuration.py for a list of all supported configuration parameters.
configuration = appifyhub.Configuration(
    host = "https://api.appifyhub.com"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure Bearer authorization (JWT): BearerAuth
configuration = appifyhub.Configuration(
    access_token = os.environ["BEARER_TOKEN"]
)

# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.MessagingApi(api_client)
    universal_id = 'universal_id_example' # str | 

    try:
        # Remove all push devices
        api_response = api_instance.remove_all_push_devices_for_user(universal_id)
        print("The response of MessagingApi->remove_all_push_devices_for_user:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->remove_all_push_devices_for_user: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 

### Return type

[**SimpleResponse**](SimpleResponse.md)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **remove_push_device**
> SimpleResponse remove_push_device(universal_id, device_id)

Remove a push device

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.simple_response import SimpleResponse
from appifyhub.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api.appifyhub.com
# See configuration.py for a list of all supported configuration parameters.
configuration = appifyhub.Configuration(
    host = "https://api.appifyhub.com"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure Bearer authorization (JWT): BearerAuth
configuration = appifyhub.Configuration(
    access_token = os.environ["BEARER_TOKEN"]
)

# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.MessagingApi(api_client)
    universal_id = 'universal_id_example' # str | 
    device_id = 'device_id_example' # str | 

    try:
        # Remove a push device
        api_response = api_instance.remove_push_device(universal_id, device_id)
        print("The response of MessagingApi->remove_push_device:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->remove_push_device: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **universal_id** | **str**|  | 
 **device_id** | **str**|  | 

### Return type

[**SimpleResponse**](SimpleResponse.md)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **send_message**
> SimpleResponse send_message(project_id, universal_id, message_send_request)

Send a message to a user

### Example

* Bearer (JWT) Authentication (BearerAuth):

```python
import appifyhub
from appifyhub.models.message_send_request import MessageSendRequest
from appifyhub.models.simple_response import SimpleResponse
from appifyhub.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api.appifyhub.com
# See configuration.py for a list of all supported configuration parameters.
configuration = appifyhub.Configuration(
    host = "https://api.appifyhub.com"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure Bearer authorization (JWT): BearerAuth
configuration = appifyhub.Configuration(
    access_token = os.environ["BEARER_TOKEN"]
)

# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.MessagingApi(api_client)
    project_id = 56 # int | 
    universal_id = 'universal_id_example' # str | 
    message_send_request = appifyhub.MessageSendRequest() # MessageSendRequest | 

    try:
        # Send a message to a user
        api_response = api_instance.send_message(project_id, universal_id, message_send_request)
        print("The response of MessagingApi->send_message:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling MessagingApi->send_message: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project_id** | **int**|  | 
 **universal_id** | **str**|  | 
 **message_send_request** | [**MessageSendRequest**](MessageSendRequest.md)|  | 

### Return type

[**SimpleResponse**](SimpleResponse.md)

### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

