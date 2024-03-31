# VariableResponse

A response containing the variable details

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**code** | **str** | The code of the variable (text to replace in templates). Does not include the {{curly braces}}. | 
**example** | **str** | The example value of the variable | 

## Example

```python
from appifyhub.models.variable_response import VariableResponse

# TODO update the JSON string below
json = "{}"
# create an instance of VariableResponse from a JSON string
variable_response_instance = VariableResponse.from_json(json)
# print the JSON string representation of the object
print(VariableResponse.to_json())

# convert the object into a dict
variable_response_dict = variable_response_instance.to_dict()
# create an instance of VariableResponse from a dict
variable_response_form_dict = variable_response.from_dict(variable_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


