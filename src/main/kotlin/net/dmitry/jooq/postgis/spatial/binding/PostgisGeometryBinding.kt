package net.dmitry.jooq.postgis.spatial.binding

import net.dmitry.jooq.postgis.spatial.converter.PostgisGeometryConverter
import org.jooq.*
import org.jooq.impl.DSL
import org.postgis.Geometry

/**
 * @author Dmitry Zhuravlev
 *         Date: 07.03.16
 */
class PostgisGeometryBinding : Binding<Any, Geometry> {

    private val geometryConverter = PostgisGeometryConverter();

    override fun converter(): Converter<Any, Geometry>? = geometryConverter

    override fun set(ctx: BindingSetStatementContext<Geometry>?) {
        ctx?.statement()?.setObject(ctx.index(), ctx.convert(converter()).value())
    }

    override fun get(ctx: BindingGetStatementContext<Geometry>?) {
        ctx?.convert(converter())?.value(ctx.statement().getObject(ctx.index()));
    }

    override fun get(ctx: BindingGetResultSetContext<Geometry>?) {
        ctx?.convert(converter())?.value(ctx.resultSet().getObject(ctx.index()));
    }

    override fun sql(ctx: BindingSQLContext<Geometry>?) {
        ctx?.render()?.visit(DSL.sql("?::geometry"))
    }

    override fun get(ctx: BindingGetSQLInputContext<Geometry>?) = throw UnsupportedOperationException()

    override fun set(ctx: BindingSetSQLOutputContext<Geometry>?) = throw UnsupportedOperationException()

    override fun register(ctx: BindingRegisterContext<Geometry>?) = throw UnsupportedOperationException()

}