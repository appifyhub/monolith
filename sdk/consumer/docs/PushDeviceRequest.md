# PushDeviceRequest

A request containing the push device details

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | The unique identifier of the device (e.g. a Firebase token) | 
**type** | [**PushDeviceType**](PushDeviceType.md) |  | 

## Example

```python
from appifyhub.models.push_device_request import PushDeviceRequest

# TODO update the JSON string below
json = "{}"
# create an instance of PushDeviceRequest from a JSON string
push_device_request_instance = PushDeviceRequest.from_json(json)
# print the JSON string representation of the object
print(PushDeviceRequest.to_json())

# convert the object into a dict
push_device_request_dict = push_device_request_instance.to_dict()
# create an instance of PushDeviceRequest from a dict
push_device_request_from_dict = PushDeviceRequest.from_dict(push_device_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


