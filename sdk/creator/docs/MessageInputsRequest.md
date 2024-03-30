# MessageInputsRequest

Dynamic message inputs for variable replacement

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**user_id** | **str** | The universal ID of the object (user ID and project ID separated by a dollar sign) | [optional] 
**project_id** | **int** | A unique identifier for this consumer project | [optional] 

## Example

```python
from appifyhub.models.message_inputs_request import MessageInputsRequest

# TODO update the JSON string below
json = "{}"
# create an instance of MessageInputsRequest from a JSON string
message_inputs_request_instance = MessageInputsRequest.from_json(json)
# print the JSON string representation of the object
print(MessageInputsRequest.to_json())

# convert the object into a dict
message_inputs_request_dict = message_inputs_request_instance.to_dict()
# create an instance of MessageInputsRequest from a dict
message_inputs_request_form_dict = message_inputs_request.from_dict(message_inputs_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


