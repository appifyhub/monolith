# PushDevicesResponse

A response containing device details of all push devices

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**devices** | [**List[PushDeviceResponse]**](PushDeviceResponse.md) |  | 

## Example

```python
from appifyhub.models.push_devices_response import PushDevicesResponse

# TODO update the JSON string below
json = "{}"
# create an instance of PushDevicesResponse from a JSON string
push_devices_response_instance = PushDevicesResponse.from_json(json)
# print the JSON string representation of the object
print(PushDevicesResponse.to_json())

# convert the object into a dict
push_devices_response_dict = push_devices_response_instance.to_dict()
# create an instance of PushDevicesResponse from a dict
push_devices_response_from_dict = PushDevicesResponse.from_dict(push_devices_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


