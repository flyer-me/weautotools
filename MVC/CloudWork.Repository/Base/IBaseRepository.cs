using System.Linq.Expressions;

namespace CloudWork.Repository.Base
{
    public interface IBaseRepository<TEntity> where TEntity : class
    {
        /// <summary>
        /// 获取所有实体
        /// </summary>
        /// <returns>实体枚举</returns>
        Task<IEnumerable<TEntity>> GetAllAsync();
        /// <summary>
        /// 根据int ID获取实体
        /// </summary>
        /// <param name="id"></param>
        /// <returns>单个实体，或null</returns>
        Task<TEntity?> GetByIdAsync(object id);

        /// <summary>
        /// 添加实体
        /// </summary>
        /// <param name="entity"></param>
        Task AddAsync(TEntity entity);
        /// <summary>
        /// 添加实体集合
        /// </summary>
        /// <param name="entities"></param>
        /// <returns></returns>
        Task AddAsync(IEnumerable<TEntity> entities);
        /// <summary>
        /// 根据ID删除实体
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        Task DeleteAsync(int id);
        /// <summary>
        /// 删除实体
        /// </summary>
        /// <param name="entity"></param>
        void Delete(TEntity entity);
        /// <summary>
        /// 删除实体集合
        /// </summary>
        /// <param name="entities"></param>
        void Delete(IEnumerable<TEntity> entities);
        /// <summary>
        /// 更新实体
        /// </summary>
        /// <param name="entity"></param>
        void Update(TEntity entity);
        /// <summary>
        /// 保存更改至数据库
        /// </summary>
        /// <returns></returns>
        Task<int> SaveAsync();

        /// <summary>
        /// 使用表达式获取 枚举泛型结果：select expression, where expression, orderbyFunc, 逗号分隔的属性名称include properties
        /// </summary>
        /// <typeparam name="TResult"></typeparam>
        /// <param name="expression"></param>
        /// <param name="whereExpression"></param>
        /// <param name="orderByFunc"></param>
        /// <param name="includeProperties"></param>
        /// <returns></returns>
        Task<IEnumerable<TResult>> GetAsync<TResult>(
            Expression<Func<TEntity, TResult>> expression,
            Expression<Func<TEntity, bool>> whereExpression,
            Func<IQueryable<TEntity>, IOrderedQueryable<TEntity>>? orderByFunc,
            string includeProperties
            );
        /// <summary>
        /// 查询是否存在符合条件的实体
        /// </summary>
        /// <param name="predicate"></param>
        /// <returns></returns>
        Task<bool> AnyAsync(Expression<Func<TEntity, bool>> predicate);
    }
}
