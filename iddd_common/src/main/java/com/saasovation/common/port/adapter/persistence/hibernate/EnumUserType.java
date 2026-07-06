//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.saasovation.common.port.adapter.persistence.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;

public class EnumUserType<E extends Enum<E>> implements UserType<E> {

    private Class<E> clazz = null;

    protected EnumUserType(Class<E> c) {
        this.clazz = c;
    }

    public int getSqlType() {
        return Types.VARCHAR;
    }

    public Class<E> returnedClass() {
        return clazz;
    }

    public E nullSafeGet(
            ResultSet resultSet,
            int position,
            WrapperOptions options)
    throws SQLException {
        String name = resultSet.getString(position);
        E result = null;
        if (!resultSet.wasNull()) {
            result = Enum.valueOf(clazz, name);
        }
        return result;
    }

    public void nullSafeSet(
            PreparedStatement preparedStatement,
            E value,
            int index,
            WrapperOptions options)
    throws SQLException {
        if (null == value) {
            preparedStatement.setNull(index, Types.VARCHAR);
        } else {
            preparedStatement.setString(index, value.name());
        }
    }

    public E deepCopy(E value) {
        return value;
    }

    public boolean isMutable() {
        return false;
    }

    public E assemble(Serializable cached, Object owner) {
         return this.returnedClass().cast(cached);
    }

    public Serializable disassemble(E value) {
        return (Serializable)value;
    }

    public E replace(E original, E target, Object owner) {
        return original;
    }
    public int hashCode(E x) {
        return x.hashCode();
    }
    public boolean equals(E x, E y) {
        if (x == y)
            return true;
        if (null == x || null == y)
            return false;
        return x.equals(y);
    }
}
