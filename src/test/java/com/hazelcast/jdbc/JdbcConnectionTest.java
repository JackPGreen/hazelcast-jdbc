/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.jdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assume.assumeFalse;

@ExtendWith(MockitoExtension.class)
public class JdbcConnectionTest {

    @Mock
    private HazelcastSqlClient client;
    @InjectMocks
    private JdbcConnection connection;

    @Test
    public void shouldNotSupportAutogeneratedKeys() {
        connection = new JdbcConnection(client);
        assertThatThrownBy(() -> connection.prepareStatement("SELECT * FROM person", new int[]{1, 2, 3}))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Auto-generated keys are not supported.");
    }

    @Test
    void shouldUnwrapConnection() {
        assertThat(connection.isWrapperFor(JdbcConnection.class)).isTrue();
        assertThat(connection.unwrap(JdbcConnection.class)).isNotNull();
    }

    @Test
    void shouldCreateStatementForSupportedParameters() throws SQLException {
        assertThat(connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)).isNotNull();
        assertThat(connection.createStatement(
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isNotNull();

        assertThat(connection.prepareStatement(
                "SELECT * FROM person", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))
                .isNotNull();
        assertThat(connection.prepareStatement(
                "SELECT * FROM person", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isNotNull();
    }

    @ParameterizedTest(name = "With ResultSet type {0}")
    @MethodSource("statementIntValues")
    void shouldValidateResultSetTypeForStatement(int resultSetType) {
        assumeFalse(resultSetType == ResultSet.TYPE_FORWARD_ONLY);

        assertThatThrownBy(() -> connection.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported ResultSet type: " + resultSetType);

        assertThatThrownBy(() -> connection.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported ResultSet type: " + resultSetType);

        assertThatThrownBy(() -> connection.prepareStatement(
                "SELECT * FROM person", resultSetType, ResultSet.CONCUR_READ_ONLY))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported ResultSet type: " + resultSetType);

        assertThatThrownBy(() -> connection.prepareStatement(
                "SELECT * FROM person", resultSetType, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported ResultSet type: " + resultSetType);
    }

    @ParameterizedTest(name = "With ResultSet concurrency {0}")
    @MethodSource("statementIntValues")
    void shouldValidateResultSetConcurrencyForStatement(int resultSetConcurrency) {
        assumeFalse(resultSetConcurrency == ResultSet.CONCUR_READ_ONLY);

        assertThatThrownBy(() -> connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, resultSetConcurrency))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported ResultSet concurrency: " + resultSetConcurrency);

        assertThatThrownBy(() -> connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, resultSetConcurrency, ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported ResultSet concurrency: " + resultSetConcurrency);

        assertThatThrownBy(() -> connection.prepareStatement(
                "SELECT * FROM person", ResultSet.TYPE_FORWARD_ONLY, resultSetConcurrency))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported ResultSet concurrency: " + resultSetConcurrency);

        assertThatThrownBy(() -> connection.prepareStatement(
                "SELECT * FROM person", ResultSet.TYPE_FORWARD_ONLY, resultSetConcurrency, ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported ResultSet concurrency: " + resultSetConcurrency);
    }

    @ParameterizedTest(name = "With ResultSet holdability {0}")
    @MethodSource("statementIntValues")
    void shouldValidateResultSetHoldabilityForStatement(int resultSetHoldability) {
        assumeFalse(resultSetHoldability == ResultSet.CLOSE_CURSORS_AT_COMMIT);

        assertThatThrownBy(() -> connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, resultSetHoldability))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported ResultSet holdability: " + resultSetHoldability);

        assertThatThrownBy(() -> connection.prepareStatement(
                "SELECT * FROM person", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, resultSetHoldability))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported ResultSet holdability: " + resultSetHoldability);
    }

    private static Stream<Arguments> statementIntValues() {
        return Stream.of(ResultSet.CONCUR_UPDATABLE,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.FETCH_UNKNOWN,
                ResultSet.FETCH_FORWARD,
                ResultSet.FETCH_REVERSE,
                ResultSet.HOLD_CURSORS_OVER_COMMIT,
                ResultSet.CLOSE_CURSORS_AT_COMMIT,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.TYPE_SCROLL_SENSITIVE)
                .map(Arguments::of);
    }
}