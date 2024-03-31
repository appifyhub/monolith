# DetectVariablesRequest

A request to detect variables in a string

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**content** | **str** | The template text content, including variables | 

## Example

```python
from appifyhub.models.detect_variables_request import DetectVariablesRequest

# TODO update the JSON string below
json = "{}"
# create an instance of DetectVariablesRequest from a JSON string
detect_variables_request_instance = DetectVariablesRequest.from_json(json)
# print the JSON string representation of the object
print(DetectVariablesRequest.to_json())

# convert the object into a dict
detect_variables_request_dict = detect_variables_request_instance.to_dict()
# create an instance of DetectVariablesRequest from a dict
detect_variables_request_form_dict = detect_variables_request.from_dict(detect_variables_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


