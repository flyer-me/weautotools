using System.Linq.Expressions;

namespace CloudWork.Repository.Base
{
    public interface IBaseRepository<TEntity> where TEntity : class
    {
        Task<IEnumerable<TEntity>> GetAllAsync();
        Task<TEntity?> GetByIdAsync(object id);
        Task AddAsync(TEntity entity);
        Task AddAsync(IEnumerable<TEntity> entities);
        Task DeleteAsync(object id);
        void Delete(TEntity entity);
        void Delete(IEnumerable<TEntity> entities);
        void Update(TEntity entity);
        Task<int> SaveAsync();

        /// <summary>
        /// Get entity by expression, where expression, order by, include properties
        /// </summary>
        /// <typeparam name="TResult"></typeparam>
        /// <param name="expression"></param>
        /// <param name="whereExpression"></param>
        /// <param name="orderBy"></param>
        /// <param name="includeProperties"></param>
        /// <returns></returns>
        Task<IEnumerable<TResult>> Get<TResult>(
            Expression<Func<TEntity, TResult>> expression,
            Expression<Func<TEntity, bool>> whereExpression,
            Func<IQueryable<TEntity>, IOrderedQueryable<TEntity>>? orderBy,
            string includeProperties
            );
    }
}
