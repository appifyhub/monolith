# appifyhub.HealthApi

All URIs are relative to *https://api.appifyhub.com*

Method | HTTP request | Description
------------- | ------------- | -------------
[**heartbeat**](HealthApi.md#heartbeat) | **GET** /heartbeat | Check the heartbeat


# **heartbeat**
> HeartbeatResponse heartbeat()

Check the heartbeat

### Example


```python
import appifyhub
from appifyhub.models.heartbeat_response import HeartbeatResponse
from appifyhub.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api.appifyhub.com
# See configuration.py for a list of all supported configuration parameters.
configuration = appifyhub.Configuration(
    host = "https://api.appifyhub.com"
)


# Enter a context with an instance of the API client
with appifyhub.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = appifyhub.HealthApi(api_client)

    try:
        # Check the heartbeat
        api_response = api_instance.heartbeat()
        print("The response of HealthApi->heartbeat:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling HealthApi->heartbeat: %s\n" % e)
```



### Parameters

This endpoint does not need any parameter.

### Return type

[**HeartbeatResponse**](HeartbeatResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |
**0** | Error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

