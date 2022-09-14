/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.features.test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeType
        implements ParameterizedType
{
    private final String typeName;
    private final Class<?> baseClass;
    private final Type[] genericClass;

    public CompositeType(Class<?> baseClass, Class<?>... genericClasses)
    {
        this.baseClass = baseClass;
        this.genericClass = genericClasses;
        List<String> generics = Arrays.asList(genericClasses)
                .stream()
                .map(Class::getName)
                .collect(Collectors.toList());
        String genericTypeString = String.join("", generics);
        this.typeName = baseClass.getName() + "<" + genericTypeString + ">";
    }

    @Override
    public String getTypeName()
    {
        return typeName;
    }

    @Override
    public Type[] getActualTypeArguments()
    {
        return genericClass;
    }

    @Override
    public Type getRawType()
    {
        return baseClass;
    }

    @Override
    public Type getOwnerType()
    {
        return null;
    }
}
