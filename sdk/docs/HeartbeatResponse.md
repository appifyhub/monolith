# HeartbeatResponse

A simple heartbeat response

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**beat_time** | **datetime** | The time the heartbeat was triggered (ISO 8601) | 
**request_ip** | **str** | The approximate IP address | [optional] 
**request_geo** | **str** | The approximate geographic location based on the IP address | [optional] 
**version** | **str** | The running version of the service | 

## Example

```python
from appifyhub.models.heartbeat_response import HeartbeatResponse

# TODO update the JSON string below
json = "{}"
# create an instance of HeartbeatResponse from a JSON string
heartbeat_response_instance = HeartbeatResponse.from_json(json)
# print the JSON string representation of the object
print(HeartbeatResponse.to_json())

# convert the object into a dict
heartbeat_response_dict = heartbeat_response_instance.to_dict()
# create an instance of HeartbeatResponse from a dict
heartbeat_response_form_dict = heartbeat_response.from_dict(heartbeat_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


