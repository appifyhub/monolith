# coding: utf-8

"""
    Appify Hub's Creator API

    The full specification of the service's API used by the project administrators.

    The version of the OpenAPI document: Latest
    Contact: contact@appifyhub.com
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


from __future__ import annotations
import json
from enum import Enum
from typing_extensions import Self


class ProjectUserIDType(str, Enum):
    """
    The type of the user ID used by this consumer project (mainly important for validation). Use \"RANDOM\" to generate a new user ID on signup, and \"CUSTOM\" to use an own/custom user ID. 
    """

    """
    allowed enum values
    """
    USERNAME = 'USERNAME'
    EMAIL = 'EMAIL'
    PHONE = 'PHONE'
    RANDOM = 'RANDOM'
    CUSTOM = 'CUSTOM'

    @classmethod
    def from_json(cls, json_str: str) -> Self:
        """Create an instance of ProjectUserIDType from a JSON string"""
        return cls(json.loads(json_str))

